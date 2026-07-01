# TicketFlow 第三阶段协同运营增强实施方案

> **面向执行代理：** 实施本计划时必须使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 按任务逐步执行。步骤使用复选框语法，便于跟踪进度。

**目标：** 在第二阶段工单闭环基础上，补齐消息中心、工单动作通知、SLA 预警升级、操作审计和验证文档，让系统具备内部试运行需要的协同和追踪能力。

**架构：** 继续采用模块化单体，不重构现有系统边界。后端复用现有 RabbitMQ 通知链路、MyBatis-Plus 持久化和 Redis 缓存；前端复用当前 Vue3、Element Plus 管理后台布局，新增独立消息中心和审计日志页面。通知幂等通过业务幂等键控制，操作审计通过 WebMvc 拦截器记录变更类请求，SLA 预警通过独立告警记录避免重复推送。

**技术栈：** Java 17、Spring Boot 3、MyBatis-Plus、MySQL、Redis、RabbitMQ、MinIO、Vue3、Element Plus、Pinia、Vue Router、Axios、Docker Compose。

---

## 一、现状判断

当前 `main` 已经合并第二阶段工单闭环能力，已覆盖登录鉴权、RBAC、系统管理、工单创建与流转、附件、规则配置、SLA 超时通知、站内信弹层、统计看板和基础集成测试。

第三阶段主要解决以下缺口：

- 消息目前只有顶部弹层，没有完整消息中心、筛选、批量已读和更稳定的幂等键。
- 工单创建、转派、接单、处理、驳回、关闭、评论等业务动作尚未形成统一通知。
- SLA 当前只做超时通知，缺少临期预警、重复扫描抑制和告警记录。
- 系统管理、规则配置、工单变更、附件变更没有统一操作审计。
- 前端缺少面向运维主管和管理员的协同运营页面。
- 集成测试覆盖了核心链路，但第三阶段新增协同能力需要补充端到端验证。

第三阶段不做多租户、复杂审批流、WebSocket 实时推送、外部短信邮件集成和工单模板设计，避免范围过大影响稳定性。

## 二、文件结构

### 后端新增文件

- `backend/src/main/java/com/ticketflow/notification/dto/NotificationQueryRequest.java`：消息中心查询 DTO。
- `backend/src/main/java/com/ticketflow/ticket/domain/TicketNotificationFactory.java`：工单动作通知事件工厂。
- `backend/src/main/java/com/ticketflow/sla/entity/SlaAlertRecord.java`：SLA 告警记录实体。
- `backend/src/main/java/com/ticketflow/sla/enums/SlaAlertType.java`：SLA 告警类型枚举。
- `backend/src/main/java/com/ticketflow/sla/mapper/SlaAlertRecordMapper.java`：SLA 告警记录 Mapper。
- `backend/src/main/java/com/ticketflow/sla/service/SlaAlertService.java`：SLA 告警记录服务接口。
- `backend/src/main/java/com/ticketflow/sla/service/impl/SlaAlertServiceImpl.java`：SLA 告警记录服务实现。
- `backend/src/main/java/com/ticketflow/audit/entity/OperationLog.java`：操作审计日志实体。
- `backend/src/main/java/com/ticketflow/audit/dto/OperationLogResponse.java`：操作审计响应 DTO。
- `backend/src/main/java/com/ticketflow/audit/mapper/OperationLogMapper.java`：操作审计 Mapper。
- `backend/src/main/java/com/ticketflow/audit/service/OperationLogService.java`：操作审计服务接口。
- `backend/src/main/java/com/ticketflow/audit/service/impl/OperationLogServiceImpl.java`：操作审计服务实现。
- `backend/src/main/java/com/ticketflow/audit/controller/OperationLogController.java`：操作审计查询接口。
- `backend/src/main/java/com/ticketflow/common/config/OperationLogInterceptor.java`：变更类请求审计拦截器。
- `backend/src/test/java/com/ticketflow/sla/job/SlaMonitorJobTest.java`：SLA 预警和超时扫描单元测试。

### 后端修改文件

- `backend/src/main/resources/schema.sql`：新增通知字段、SLA 告警记录表、操作审计表。
- `backend/src/main/resources/data.sql`：新增消息中心、审计日志菜单和权限。
- `backend/src/main/java/com/ticketflow/notification/dto/NotificationEvent.java`：增加通知级别和幂等键字段。
- `backend/src/main/java/com/ticketflow/notification/dto/NotificationMessageResponse.java`：增加通知级别、幂等键、已读时间字段。
- `backend/src/main/java/com/ticketflow/notification/entity/NotificationMessage.java`：增加通知级别、幂等键、已读时间字段。
- `backend/src/main/java/com/ticketflow/notification/service/NotificationService.java`：增加筛选查询和批量已读方法。
- `backend/src/main/java/com/ticketflow/notification/service/impl/NotificationServiceImpl.java`：实现筛选、批量已读、幂等键落库。
- `backend/src/main/java/com/ticketflow/notification/controller/NotificationController.java`：扩展消息中心接口。
- `backend/src/main/java/com/ticketflow/sla/domain/SlaNotificationFactory.java`：增加临期预警通知。
- `backend/src/main/java/com/ticketflow/sla/job/SlaMonitorJob.java`：接入告警记录和临期预警。
- `backend/src/main/java/com/ticketflow/ticket/service/impl/TicketServiceImpl.java`：在关键工单动作后发布通知。
- `backend/src/main/java/com/ticketflow/common/config/WebMvcConfig.java`：注册操作审计拦截器。
- `backend/src/test/java/com/ticketflow/integration/AuthIntegrationTest.java`：补充消息、工单通知、操作审计集成测试。

### 前端新增文件

- `frontend/src/views/MessageCenterView.vue`：消息中心页面。
- `frontend/src/views/AuditLogView.vue`：操作审计日志页面。
- `frontend/src/api/audit.js`：操作审计接口封装。

### 前端修改文件

- `frontend/src/api/message.js`：增加筛选查询和批量已读方法。
- `frontend/src/router/index.js`：新增消息中心、审计日志路由。
- `frontend/src/layouts/MainLayout.vue`：消息弹层增加跳转消息中心入口。
- `frontend/src/styles.css`：补充消息中心和审计日志页面样式。
- `README.md`：补充第三阶段能力说明和验证方式。

---

## 三、任务拆分

### Task 1：消息中心模型和接口增强

**Files:**

- Modify: `backend/src/main/resources/schema.sql`
- Modify: `backend/src/main/java/com/ticketflow/notification/dto/NotificationEvent.java`
- Modify: `backend/src/main/java/com/ticketflow/notification/dto/NotificationMessageResponse.java`
- Create: `backend/src/main/java/com/ticketflow/notification/dto/NotificationQueryRequest.java`
- Modify: `backend/src/main/java/com/ticketflow/notification/entity/NotificationMessage.java`
- Modify: `backend/src/main/java/com/ticketflow/notification/service/NotificationService.java`
- Modify: `backend/src/main/java/com/ticketflow/notification/service/impl/NotificationServiceImpl.java`
- Modify: `backend/src/main/java/com/ticketflow/notification/controller/NotificationController.java`
- Modify: `backend/src/test/java/com/ticketflow/integration/AuthIntegrationTest.java`

- [ ] **Step 1：先写消息中心集成测试**

在 `AuthIntegrationTest` 新增测试，验证消息可按未读、业务类型筛选，并支持批量已读。

```java
@Test
@SuppressWarnings("unchecked")
void 消息中心支持筛选和批量已读() throws Exception {
    notificationPublisher.publish(new NotificationEvent(
            1L,
            "第三阶段未读通知",
            "验证消息中心筛选和批量已读",
            "PHASE3_MESSAGE",
            93001L,
            "INFO",
            "PHASE3_MESSAGE:93001"
    ));

    Map<String, Object> message = waitForMessage("PHASE3_MESSAGE", 93001L);
    assertThat(message).isNotNull();
    assertThat(message.get("readFlag")).isEqualTo(0);

    ResponseEntity<Map> unreadResponse = restTemplate.exchange(
            url("/api/messages?pageNo=1&pageSize=10&readFlag=0&businessType=PHASE3_MESSAGE"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            Map.class
    );
    Map<String, Object> unreadPage = data(unreadResponse);
    List<Map<String, Object>> unreadRecords = (List<Map<String, Object>>) unreadPage.get("records");
    assertThat(unreadRecords).anySatisfy(record -> {
        assertThat(record.get("businessType")).isEqualTo("PHASE3_MESSAGE");
        assertThat(record.get("dedupeKey")).isEqualTo("PHASE3_MESSAGE:93001");
    });

    ResponseEntity<Map> readAllResponse = restTemplate.postForEntity(
            url("/api/messages/read-all"),
            new HttpEntity<>(Map.of("businessType", "PHASE3_MESSAGE"), authHeaders()),
            Map.class
    );
    assertThat(readAllResponse.getBody()).isNotNull();
    assertThat(readAllResponse.getBody().get("code")).isEqualTo(0);

    ResponseEntity<Map> unreadAfterResponse = restTemplate.exchange(
            url("/api/messages?pageNo=1&pageSize=10&readFlag=0&businessType=PHASE3_MESSAGE"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            Map.class
    );
    Map<String, Object> unreadAfterPage = data(unreadAfterResponse);
    List<Map<String, Object>> unreadAfterRecords = (List<Map<String, Object>>) unreadAfterPage.get("records");
    assertThat(unreadAfterRecords).noneMatch(record -> "PHASE3_MESSAGE".equals(record.get("businessType")));
}
```

- [ ] **Step 2：运行测试并确认失败**

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=AuthIntegrationTest#消息中心支持筛选和批量已读 test
```

Expected: 编译失败或接口返回失败，原因是 `NotificationEvent` 新字段、`dedupeKey` 响应字段和 `/api/messages/read-all` 接口尚未实现。

- [ ] **Step 3：扩展消息表结构**

在 `schema.sql` 的 `notification_message` 表中增加字段和唯一索引。

```sql
CREATE TABLE IF NOT EXISTS notification_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receiver_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    business_type VARCHAR(64),
    business_id BIGINT,
    level VARCHAR(32) NOT NULL DEFAULT 'INFO',
    dedupe_key VARCHAR(200) NOT NULL,
    read_flag TINYINT NOT NULL DEFAULT 0,
    read_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_notification_receiver (receiver_id, read_flag),
    KEY idx_notification_business (business_type, business_id),
    UNIQUE KEY uk_notification_dedupe (receiver_id, dedupe_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 4：扩展通知 DTO**

`NotificationEvent` 保持中文注释，增加 `level` 和 `dedupeKey`。

```java
public record NotificationEvent(
        Long receiverId,
        String title,
        String content,
        String businessType,
        Long businessId,
        String level,
        String dedupeKey
) implements Serializable {

    private static final long serialVersionUID = 1L;
}
```

新增 `NotificationQueryRequest`，用于承载消息中心筛选条件。

```java
/**
 * 消息中心查询 DTO。
 *
 * @param readFlag 已读状态，0 表示未读，1 表示已读，空值表示全部。
 * @param businessType 业务类型，例如 TICKET_ASSIGNED、SLA_RESOLVE_OVERDUE。
 * @param keyword 标题或内容关键字。
 */
public record NotificationQueryRequest(
        Integer readFlag,
        String businessType,
        String keyword
) {
}
```

- [ ] **Step 5：实现消息筛选和批量已读**

`NotificationService` 增加方法：

```java
PageResult<NotificationMessageResponse> pageCurrentUserMessages(
        long pageNo,
        long pageSize,
        NotificationQueryRequest request
);

void markAllRead(NotificationQueryRequest request);
```

`NotificationController` 增加查询参数和批量已读接口：

```java
@GetMapping
public ApiResult<PageResult<NotificationMessageResponse>> page(
        @RequestParam(defaultValue = "1") long pageNo,
        @RequestParam(defaultValue = "10") long pageSize,
        NotificationQueryRequest request
) {
    return ApiResult.success(notificationService.pageCurrentUserMessages(pageNo, pageSize, request));
}

@PostMapping("/read-all")
public ApiResult<Void> markAllRead(@RequestBody(required = false) NotificationQueryRequest request) {
    notificationService.markAllRead(request == null ? new NotificationQueryRequest(null, null, null) : request);
    return ApiResult.success();
}
```

- [ ] **Step 6：运行消息中心测试通过**

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=AuthIntegrationTest#消息中心支持筛选和批量已读 test
```

Expected: 测试通过，未读消息可以筛选，批量已读后未读筛选结果不再包含该业务类型消息。

### Task 2：工单动作通知

**Files:**

- Create: `backend/src/main/java/com/ticketflow/ticket/domain/TicketNotificationFactory.java`
- Modify: `backend/src/main/java/com/ticketflow/ticket/service/impl/TicketServiceImpl.java`
- Modify: `backend/src/test/java/com/ticketflow/integration/AuthIntegrationTest.java`

- [ ] **Step 1：先写工单通知集成测试**

新增测试覆盖创建自动派单、处理提交和评论通知。

```java
@Test
@SuppressWarnings("unchecked")
void 工单关键动作会生成对应站内信() throws Exception {
    String employeeToken = loginAndToken("employee", "123456");
    String engineerToken = loginAndToken("ops_engineer", "123456");

    Map<String, Object> ticket = postData("/api/tickets", Map.of(
            "title", "第三阶段通知工单",
            "description", "验证工单动作通知",
            "categoryId", 1,
            "priority", "MEDIUM"
    ), employeeToken);
    Long ticketId = numberValue(ticket.get("id")).longValue();

    Map<String, Object> assignedMessage = waitForMessage("TICKET_ASSIGNED", ticketId);
    assertThat(assignedMessage).isNotNull();
    assertThat(assignedMessage.get("title")).isEqualTo("你有新的待接单工单");

    postData("/api/tickets/" + ticketId + "/accept", Map.of(), engineerToken);
    postData("/api/tickets/" + ticketId + "/process", Map.of(
            "result", "第三阶段处理完成"
    ), engineerToken);

    Map<String, Object> processMessage = waitForMessage("TICKET_PENDING_CONFIRM", ticketId);
    assertThat(processMessage).isNotNull();
    assertThat(processMessage.get("title")).isEqualTo("工单等待确认");

    postData("/api/tickets/" + ticketId + "/comments", Map.of(
            "content", "第三阶段补充评论",
            "internalOnly", false
    ), employeeToken);
    Map<String, Object> commentMessage = waitForMessage("TICKET_COMMENTED", ticketId);
    assertThat(commentMessage).isNotNull();
}
```

- [ ] **Step 2：运行测试并确认失败**

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=AuthIntegrationTest#工单关键动作会生成对应站内信 test
```

Expected: 测试失败，因为工单动作尚未发布 `TICKET_ASSIGNED`、`TICKET_PENDING_CONFIRM` 和 `TICKET_COMMENTED` 通知事件。

- [ ] **Step 3：新增工单通知事件工厂**

创建 `TicketNotificationFactory`，集中维护通知文案和幂等键。

```java
public final class TicketNotificationFactory {

    private TicketNotificationFactory() {
    }

    public static NotificationEvent assigned(Ticket ticket) {
        return new NotificationEvent(
                ticket.getAssigneeId(),
                "你有新的待接单工单",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已分派给你，请及时接单。",
                "TICKET_ASSIGNED",
                ticket.getId(),
                "INFO",
                "TICKET_ASSIGNED:" + ticket.getId() + ":" + ticket.getAssigneeId()
        );
    }

    public static NotificationEvent pendingConfirm(Ticket ticket) {
        return new NotificationEvent(
                ticket.getCreatorId(),
                "工单等待确认",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已提交处理结果，请确认是否关闭。",
                "TICKET_PENDING_CONFIRM",
                ticket.getId(),
                "INFO",
                "TICKET_PENDING_CONFIRM:" + ticket.getId() + ":" + ticket.getUpdatedAt()
        );
    }

    public static NotificationEvent commented(Ticket ticket, Long receiverId, Long commentId) {
        return new NotificationEvent(
                receiverId,
                "工单有新评论",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」有新的沟通记录。",
                "TICKET_COMMENTED",
                ticket.getId(),
                "INFO",
                "TICKET_COMMENTED:" + ticket.getId() + ":" + commentId
        );
    }
}
```

- [ ] **Step 4：在工单服务发布通知**

在 `TicketServiceImpl` 注入 `NotificationPublisher`，在以下动作后发布通知：

- `applyAutoDispatch` 成功分派后通知处理人。
- `transfer` 成功后通知新处理人。
- `accept` 成功后通知提交人。
- `submitResult` 成功后通知提交人确认。
- `reject` 成功后通知处理人重新处理。
- `confirmClose` 成功后通知处理人。
- `cancel` 成功后通知处理人或提交人以外的参与人。
- `addComment` 成功后通知提交人和处理人中除当前操作人以外的用户。

发布通知时使用小方法包裹，避免单条通知异常影响工单主事务：

```java
private void publishNotification(NotificationEvent event) {
    if (event == null || event.receiverId() == null) {
        return;
    }
    notificationPublisher.publish(event);
}
```

- [ ] **Step 5：运行工单通知测试通过**

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=AuthIntegrationTest#工单关键动作会生成对应站内信 test
```

Expected: 测试通过，工单关键动作可通过 RabbitMQ 异步生成站内信。

### Task 3：SLA 临期预警和重复告警控制

**Files:**

- Modify: `backend/src/main/resources/schema.sql`
- Create: `backend/src/main/java/com/ticketflow/sla/entity/SlaAlertRecord.java`
- Create: `backend/src/main/java/com/ticketflow/sla/enums/SlaAlertType.java`
- Create: `backend/src/main/java/com/ticketflow/sla/mapper/SlaAlertRecordMapper.java`
- Create: `backend/src/main/java/com/ticketflow/sla/service/SlaAlertService.java`
- Create: `backend/src/main/java/com/ticketflow/sla/service/impl/SlaAlertServiceImpl.java`
- Modify: `backend/src/main/java/com/ticketflow/sla/domain/SlaNotificationFactory.java`
- Modify: `backend/src/main/java/com/ticketflow/sla/job/SlaMonitorJob.java`
- Create: `backend/src/test/java/com/ticketflow/sla/job/SlaMonitorJobTest.java`

- [ ] **Step 1：先写 SLA 扫描测试**

创建 `SlaMonitorJobTest`，验证同一工单同一告警类型只发布一次。

```java
class SlaMonitorJobTest {

    private final TicketMapper ticketMapper = mock(TicketMapper.class);
    private final NotificationPublisher notificationPublisher = mock(NotificationPublisher.class);
    private final SlaAlertService slaAlertService = mock(SlaAlertService.class);

    @Test
    void 临期和超时告警只在首次命中时发布() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTicketNo("TF20260701001");
        ticket.setTitle("SLA 告警测试");
        ticket.setCreatorId(2L);
        ticket.setAssigneeId(3L);
        ticket.setStatus(TicketStatus.PROCESSING);
        ticket.setResponseDeadline(LocalDateTime.now().minusMinutes(1));
        ticket.setResolveDeadline(LocalDateTime.now().plusMinutes(30));

        when(ticketMapper.selectList(any())).thenReturn(List.of(ticket));
        when(slaAlertService.markPublishedIfAbsent(100L, SlaAlertType.RESPONSE_OVERDUE)).thenReturn(true);
        when(slaAlertService.markPublishedIfAbsent(100L, SlaAlertType.RESOLVE_WARNING)).thenReturn(false);

        SlaMonitorJob job = new SlaMonitorJob(ticketMapper, notificationPublisher, slaAlertService);
        job.scanOverdueTickets();

        verify(notificationPublisher, times(1)).publish(any(NotificationEvent.class));
    }
}
```

- [ ] **Step 2：运行测试并确认失败**

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=SlaMonitorJobTest test
```

Expected: 编译失败，因为 `SlaAlertService`、`SlaAlertType` 和新构造方法尚未实现。

- [ ] **Step 3：新增 SLA 告警记录表**

在 `schema.sql` 增加：

```sql
CREATE TABLE IF NOT EXISTS sla_alert_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    alert_type VARCHAR(64) NOT NULL,
    published_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sla_alert_ticket_type (ticket_id, alert_type),
    KEY idx_sla_alert_ticket (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 4：实现告警记录服务**

`SlaAlertType`：

```java
public enum SlaAlertType {
    RESPONSE_WARNING,
    RESPONSE_OVERDUE,
    RESOLVE_WARNING,
    RESOLVE_OVERDUE
}
```

`SlaAlertService`：

```java
/**
 * SLA 告警记录服务。
 *
 * <p>用于判断某个工单的某类 SLA 告警是否已经发布，避免定时任务重复推送。</p>
 */
public interface SlaAlertService {

    boolean markPublishedIfAbsent(Long ticketId, SlaAlertType alertType);
}
```

- [ ] **Step 5：扩展 SLA 通知工厂和扫描任务**

`SlaNotificationFactory` 增加 `responseWarning` 和 `resolveWarning` 方法，业务类型分别为 `SLA_RESPONSE_WARNING`、`SLA_RESOLVE_WARNING`。

`SlaMonitorJob` 判断规则：

- 响应截止已过且未响应：发布 `RESPONSE_OVERDUE`。
- 响应截止剩余不超过 30 分钟且未响应：发布 `RESPONSE_WARNING`。
- 处理截止已过且未关闭：发布 `RESOLVE_OVERDUE`。
- 处理截止剩余不超过 120 分钟且未关闭：发布 `RESOLVE_WARNING`。

每次发布前先调用：

```java
if (slaAlertService.markPublishedIfAbsent(ticket.getId(), SlaAlertType.RESPONSE_OVERDUE)) {
    notificationPublisher.publish(SlaNotificationFactory.responseOverdue(ticket));
}
```

- [ ] **Step 6：运行 SLA 扫描测试通过**

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=SlaMonitorJobTest test
```

Expected: 测试通过，同一工单同一 SLA 告警类型只发布一次。

### Task 4：操作审计日志

**Files:**

- Modify: `backend/src/main/resources/schema.sql`
- Modify: `backend/src/main/resources/data.sql`
- Create: `backend/src/main/java/com/ticketflow/audit/entity/OperationLog.java`
- Create: `backend/src/main/java/com/ticketflow/audit/dto/OperationLogResponse.java`
- Create: `backend/src/main/java/com/ticketflow/audit/mapper/OperationLogMapper.java`
- Create: `backend/src/main/java/com/ticketflow/audit/service/OperationLogService.java`
- Create: `backend/src/main/java/com/ticketflow/audit/service/impl/OperationLogServiceImpl.java`
- Create: `backend/src/main/java/com/ticketflow/audit/controller/OperationLogController.java`
- Create: `backend/src/main/java/com/ticketflow/common/config/OperationLogInterceptor.java`
- Modify: `backend/src/main/java/com/ticketflow/common/config/WebMvcConfig.java`
- Modify: `backend/src/test/java/com/ticketflow/integration/AuthIntegrationTest.java`

- [ ] **Step 1：先写操作审计集成测试**

在 `AuthIntegrationTest` 新增测试，验证变更类接口会写审计日志。

```java
@Test
@SuppressWarnings("unchecked")
void 变更类接口会写入操作审计日志() {
    Map<String, Object> ticket = postData("/api/tickets", Map.of(
            "title", "第三阶段审计工单",
            "description", "验证操作审计",
            "categoryId", 1,
            "priority", "LOW"
    ));
    Long ticketId = numberValue(ticket.get("id")).longValue();

    ResponseEntity<Map> logResponse = restTemplate.exchange(
            url("/api/audit/logs?pageNo=1&pageSize=20&keyword=/api/tickets"),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            Map.class
    );
    Map<String, Object> page = data(logResponse);
    List<Map<String, Object>> records = (List<Map<String, Object>>) page.get("records");
    assertThat(records).anySatisfy(record -> {
        assertThat(record.get("requestMethod")).isEqualTo("POST");
        assertThat(record.get("requestUri").toString()).contains("/api/tickets");
        assertThat(record.get("operatorId")).isEqualTo(1);
    });
    assertThat(ticketId).isPositive();
}
```

- [ ] **Step 2：运行测试并确认失败**

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=AuthIntegrationTest#变更类接口会写入操作审计日志 test
```

Expected: 测试失败，因为 `/api/audit/logs` 和审计落库尚未实现。

- [ ] **Step 3：新增操作审计表**

在 `schema.sql` 增加：

```sql
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT,
    operator_name VARCHAR(64),
    request_method VARCHAR(16) NOT NULL,
    request_uri VARCHAR(500) NOT NULL,
    query_string VARCHAR(1000),
    client_ip VARCHAR(64),
    success TINYINT NOT NULL DEFAULT 1,
    error_message VARCHAR(1000),
    duration_ms BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_operation_log_operator (operator_id),
    KEY idx_operation_log_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 4：新增审计菜单和权限**

在 `data.sql` 增加菜单：

```sql
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, type, path, component, icon, permission, sort_order, visible)
VALUES
    (30, 0, '消息中心', 'MENU', '/messages', 'message/index', 'Bell', 'message:view', 6, 1),
    (31, 8, '审计日志', 'MENU', '/system/audit-logs', 'system/audit-log', 'DocumentChecked', 'audit:log:list', 5, 1);
```

管理员角色绑定 `30`、`31`；运维主管绑定 `30`；员工和运维工程师绑定 `30`。

- [ ] **Step 5：实现审计拦截器**

`OperationLogInterceptor` 只记录 `/api/**` 下的 `POST`、`PUT`、`DELETE` 请求，避免查询接口产生过多日志。

```java
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {

    private final OperationLogService operationLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("operationStartTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        if (!shouldRecord(request)) {
            return;
        }
        operationLogService.record(request, response, exception);
    }

    private boolean shouldRecord(HttpServletRequest request) {
        String method = request.getMethod();
        return request.getRequestURI().startsWith("/api/")
                && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method));
    }
}
```

- [ ] **Step 6：实现审计查询接口**

`OperationLogController`：

```java
@RestController
@RequestMapping("/api/audit/logs")
@RequiredArgsConstructor
@RequirePermission("audit:log:list")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public ApiResult<PageResult<OperationLogResponse>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResult.success(operationLogService.pageLogs(pageNo, pageSize, keyword));
    }
}
```

- [ ] **Step 7：注册审计拦截器并运行测试通过**

`WebMvcConfig` 构造器增加 `OperationLogInterceptor`，并在登录和权限拦截器之后注册。

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=AuthIntegrationTest#变更类接口会写入操作审计日志 test
```

Expected: 测试通过，创建工单后可在审计日志接口查询到 POST `/api/tickets` 操作。

### Task 5：前端消息中心和审计日志页面

**Files:**

- Modify: `frontend/src/api/message.js`
- Create: `frontend/src/api/audit.js`
- Create: `frontend/src/views/MessageCenterView.vue`
- Create: `frontend/src/views/AuditLogView.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/styles.css`

- [ ] **Step 1：扩展前端消息 API**

`frontend/src/api/message.js` 增加：

```javascript
export function markAllMessagesRead(data = {}) {
  return http.post('/messages/read-all', data)
}
```

保留 `pageMessages(params)`，传入 `readFlag`、`businessType`、`keyword`。

- [ ] **Step 2：新增审计 API**

创建 `frontend/src/api/audit.js`。

```javascript
import http from './http'

export function pageOperationLogs(params) {
  return http.get('/audit/logs', { params })
}
```

- [ ] **Step 3：新增消息中心页面**

`MessageCenterView.vue` 页面能力：

- 顶部筛选：关键字、已读状态、业务类型。
- 表格列：标题、内容、级别、业务类型、已读状态、创建时间。
- 操作：单条标记已读、全部标记已读、关联工单跳转。

页面调用：

```javascript
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  readFlag: '',
  businessType: ''
})
```

- [ ] **Step 4：新增审计日志页面**

`AuditLogView.vue` 页面能力：

- 顶部筛选：关键字。
- 表格列：操作人、请求方法、请求地址、客户端 IP、结果、耗时、操作时间。
- 只提供查询，不提供删除，避免审计记录被前端误删。

- [ ] **Step 5：新增路由和顶部入口**

`router/index.js` 增加：

```javascript
{
  path: 'messages',
  name: 'messages',
  meta: { title: '消息中心', icon: 'Bell' },
  component: () => import('../views/MessageCenterView.vue')
},
{
  path: 'system/audit-logs',
  name: 'audit-logs',
  meta: { title: '审计日志', icon: 'DocumentChecked' },
  component: () => import('../views/AuditLogView.vue')
}
```

`MainLayout.vue` 消息弹层底部增加“进入消息中心”按钮，跳转 `/messages`。

- [ ] **Step 6：运行前端构建**

Run:

```powershell
cd frontend
npm run build
```

Expected: Vite 构建成功生成 `frontend/dist`，控制台没有 Vue 编译错误。

### Task 6：全量验证和文档更新

**Files:**

- Modify: `README.md`
- Modify: `backend/src/test/java/com/ticketflow/integration/AuthIntegrationTest.java`

- [ ] **Step 1：运行后端全量测试**

Run:

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' test
```

Expected: 所有单元测试通过；Docker daemon 可用时，Testcontainers 集成测试也通过。

- [ ] **Step 2：运行前端构建**

Run:

```powershell
cd frontend
npm run build
```

Expected: 构建成功，`frontend/dist` 生成。

- [ ] **Step 3：更新 README 当前进展**

在 README 的“当前进展”增加第三阶段条目：

```markdown
- 消息中心支持筛选、批量已读和工单关联跳转
- 工单关键动作会通过 RabbitMQ 异步生成站内信
- SLA 支持临期预警、超时提醒和重复告警控制
- 操作审计记录变更类接口的操作人、请求地址、结果和耗时
```

- [ ] **Step 4：检查中文编码**

Run:

```powershell
rg -n "\x{FFFD}" README.md docs backend/src/main frontend/src
```

Expected: 不出现乱码替换符；同时人工检查本阶段新增文档和代码，不保留未完成占位词。

- [ ] **Step 5：执行提交前检查**

Run:

```powershell
git status --short
```

Expected: 只包含第三阶段相关文件变更。提交和推送需等待用户输入 `/b` 后再执行。

---

## 四、验收标准

- 后端消息接口支持未读状态、业务类型、关键字筛选，并支持当前用户批量已读。
- 工单创建自动派单、转派、接单、处理、驳回、关闭、取消、评论会生成对应站内信。
- SLA 定时任务支持响应临期、响应超时、处理临期、处理超时四类告警，并对同一工单同一告警类型只发布一次。
- 操作审计可以记录所有 `/api/**` 下的 `POST`、`PUT`、`DELETE` 请求，并可由管理员查询。
- 前端新增消息中心和审计日志页面，顶部消息弹层可以跳转消息中心。
- `mvn test` 和 `npm run build` 均通过。
- 新增 DTO、impl、实体类均保留详细中文注释。
- 不把现有中文注释、接口文案、字段含义改成英文，不引入乱码。

## 五、执行建议

建议按任务顺序执行，每完成一个任务先跑对应单测或构建，再进入下一任务。第三阶段涉及数据库表结构、后端接口、前端页面和集成测试，若执行过程中出现现有业务行为冲突，应先说明冲突点和调整方案，得到确认后再修改业务逻辑。
