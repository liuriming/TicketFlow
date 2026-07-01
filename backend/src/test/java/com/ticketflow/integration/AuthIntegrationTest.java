package com.ticketflow.integration;

import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.notification.service.NotificationPublisher;
import com.ticketflow.system.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 登录鉴权集成测试。
 *
 * <p>使用 Testcontainers 启动 MySQL、Redis 和 RabbitMQ，验证数据库初始化、登录 token 写入 Redis、
 * 携带 token 获取当前用户信息的关键链路。</p>
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("ticketflow")
            .withUsername("ticketflow")
            .withPassword("ticketflow123");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "ticketflow123");

    @Container
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.0-management"));

    @Container
    static final GenericContainer<?> MINIO = new GenericContainer<>(DockerImageName.parse("minio/minio:RELEASE.2025-04-22T22-12-26Z"))
            .withEnv("MINIO_ROOT_USER", "ticketflow")
            .withEnv("MINIO_ROOT_PASSWORD", "ticketflow123")
            .withCommand("server", "/data")
            .withExposedPorts(9000);

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private NotificationPublisher notificationPublisher;

    private String token;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "ticketflow123");
        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
        registry.add("minio.endpoint", () -> "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000));
        registry.add("minio.access-key", () -> "ticketflow");
        registry.add("minio.secret-key", () -> "ticketflow123");
        registry.add("minio.bucket", () -> "ticketflow");
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @BeforeEach
    void login() {
        token = loginAndToken();
    }

    @Test
    @SuppressWarnings("unchecked")
    void 管理员登录后可以携带Token获取当前用户信息() {
        ResponseEntity<Map> meResponse = restTemplate.exchange(
                url("/api/auth/me"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );

        assertThat(meResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> meBody = meResponse.getBody();
        assertThat(meBody).isNotNull();
        assertThat(meBody.get("code")).isEqualTo(0);
        Map<String, Object> meData = (Map<String, Object>) meBody.get("data");
        assertThat(meData.get("username")).isEqualTo("admin");
    }

    @Test
    void 登录Token会写入Redis缓存() {
        Boolean exists = redisTemplate.hasKey("ticketflow:login:" + token);

        assertThat(exists).isTrue();
    }

    @Test
    void 当前用户权限快照会写入Redis缓存() {
        restTemplate.exchange(
                url("/api/auth/me"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );

        Boolean exists = redisTemplate.hasKey("ticketflow:permission:user:1");
        assertThat(exists).isTrue();
    }

    @Test
    void 接口权限会阻止员工访问系统管理接口并允许管理员访问() {
        String employeeToken = loginAndToken("employee", "123456");

        ResponseEntity<Map> forbiddenResponse = restTemplate.exchange(
                url("/api/system/users"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(employeeToken)),
                Map.class
        );
        assertThat(forbiddenResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(forbiddenResponse.getBody()).isNotNull();
        assertThat(forbiddenResponse.getBody().get("code")).isEqualTo(403);

        ResponseEntity<Map> adminResponse = restTemplate.exchange(
                url("/api/system/users"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );
        assertThat(adminResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(adminResponse.getBody()).isNotNull();
        assertThat(adminResponse.getBody().get("code")).isEqualTo(0);
    }

    @Test
    void 员工提交工单后自动派给工程师并完成闭环() {
        String employeeToken = loginAndToken("employee", "123456");
        String engineerToken = loginAndToken("ops_engineer", "123456");

        Map<String, Object> created = postData("/api/tickets", Map.of(
                "title", "集成测试工单",
                "description", "验证工单闭环",
                "categoryId", 1,
                "priority", "MEDIUM"
        ), employeeToken);
        Long ticketId = numberValue(created.get("id")).longValue();
        assertThat(created.get("status")).isEqualTo("PENDING_ACCEPT");
        assertThat(numberValue(created.get("assigneeId")).longValue()).isEqualTo(3L);

        ResponseEntity<Map> forbiddenTransfer = postRaw("/api/tickets/" + ticketId + "/transfer", Map.of(
                "assigneeId", 1,
                "reason", "员工尝试转派"
        ), employeeToken);
        assertThat(forbiddenTransfer.getBody()).isNotNull();
        assertThat(forbiddenTransfer.getBody().get("code")).isEqualTo(403);

        Map<String, Object> accepted = postData("/api/tickets/" + ticketId + "/accept", Map.of(), engineerToken);
        assertThat(accepted.get("status")).isEqualTo("PROCESSING");

        Map<String, Object> processed = postData("/api/tickets/" + ticketId + "/process", Map.of(
                "result", "集成测试处理完成"
        ), engineerToken);
        assertThat(processed.get("status")).isEqualTo("PENDING_CONFIRM");

        Map<String, Object> closed = postData("/api/tickets/" + ticketId + "/confirm-close", Map.of(
                "remark", "集成测试确认关闭"
        ), employeeToken);
        assertThat(closed.get("status")).isEqualTo("CLOSED");
    }

    @Test
    @SuppressWarnings("unchecked")
    void 员工可以评论取消和驳回自己的工单() {
        String employeeToken = loginAndToken("employee", "123456");
        String engineerToken = loginAndToken("ops_engineer", "123456");

        Map<String, Object> commentTicket = postData("/api/tickets", Map.of(
                "title", "评论集成测试工单",
                "description", "验证评论写入详情",
                "categoryId", 1,
                "priority", "LOW"
        ), employeeToken);
        Long commentTicketId = numberValue(commentTicket.get("id")).longValue();
        Map<String, Object> commented = postData("/api/tickets/" + commentTicketId + "/comments", Map.of(
                "content", "补充一条沟通记录",
                "internalOnly", false
        ), employeeToken);
        List<Map<String, Object>> comments = (List<Map<String, Object>>) commented.get("comments");
        assertThat(comments).anySatisfy(comment -> assertThat(comment.get("content")).isEqualTo("补充一条沟通记录"));

        Map<String, Object> cancelTicket = postData("/api/tickets", Map.of(
                "title", "取消集成测试工单",
                "description", "验证创建人取消",
                "categoryId", 1,
                "priority", "LOW"
        ), employeeToken);
        Long cancelTicketId = numberValue(cancelTicket.get("id")).longValue();
        Map<String, Object> canceled = postData("/api/tickets/" + cancelTicketId + "/cancel", Map.of(
                "remark", "员工确认不需要处理"
        ), employeeToken);
        assertThat(canceled.get("status")).isEqualTo("CANCELED");

        Map<String, Object> rejectTicket = postData("/api/tickets", Map.of(
                "title", "驳回集成测试工单",
                "description", "验证处理结果驳回",
                "categoryId", 1,
                "priority", "LOW"
        ), employeeToken);
        Long rejectTicketId = numberValue(rejectTicket.get("id")).longValue();
        postData("/api/tickets/" + rejectTicketId + "/accept", Map.of(), engineerToken);
        postData("/api/tickets/" + rejectTicketId + "/process", Map.of(
                "result", "请员工确认"
        ), engineerToken);
        Map<String, Object> rejected = postData("/api/tickets/" + rejectTicketId + "/reject", Map.of(
                "remark", "处理说明不完整"
        ), employeeToken);
        assertThat(rejected.get("status")).isEqualTo("REJECTED");
    }

    @Test
    @SuppressWarnings("unchecked")
    void 二阶段支撑接口返回展示名称允许动作和真实统计数据() {
        String employeeToken = loginAndToken("employee", "123456");
        Map<String, Object> created = postData("/api/tickets", Map.of(
                "title", "二阶段展示字段工单",
                "description", "验证列表、详情、工作台和报表支撑接口",
                "categoryId", 1,
                "priority", "HIGH"
        ), employeeToken);
        Long ticketId = numberValue(created.get("id")).longValue();

        ResponseEntity<Map> listResponse = restTemplate.exchange(
                url("/api/tickets?pageNo=1&pageSize=10&keyword=二阶段展示字段工单"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(employeeToken)),
                Map.class
        );
        Map<String, Object> page = data(listResponse);
        List<Map<String, Object>> records = (List<Map<String, Object>>) page.get("records");
        Map<String, Object> listItem = records.stream()
                .filter(record -> ticketId.equals(numberValue(record.get("id")).longValue()))
                .findFirst()
                .orElseThrow();
        assertThat(listItem.get("categoryName")).isEqualTo("网络故障");
        assertThat(listItem.get("creatorName")).isEqualTo("报修员工");
        assertThat(listItem.get("creatorDeptName")).isEqualTo("行政部");
        assertThat(listItem.get("assigneeName")).isEqualTo("运维工程师");
        assertThat((List<String>) listItem.get("allowedActions")).contains("CANCEL", "COMMENT", "UPLOAD_ATTACHMENT");

        ResponseEntity<Map> detailResponse = restTemplate.exchange(
                url("/api/tickets/" + ticketId),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(employeeToken)),
                Map.class
        );
        Map<String, Object> detail = data(detailResponse);
        assertThat(detail.get("categoryName")).isEqualTo("网络故障");
        assertThat(detail.get("creatorName")).isEqualTo("报修员工");
        assertThat(detail.get("creatorDeptName")).isEqualTo("行政部");
        assertThat(detail.get("assigneeName")).isEqualTo("运维工程师");
        assertThat((List<String>) detail.get("allowedActions")).contains("CANCEL", "COMMENT", "UPLOAD_ATTACHMENT");

        ResponseEntity<Map> userOptionsResponse = restTemplate.exchange(
                url("/api/system/users/options"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );
        List<Map<String, Object>> userOptions = data(userOptionsResponse);
        assertThat(userOptions).anySatisfy(option -> {
            assertThat(option.get("realName")).isEqualTo("运维工程师");
            assertThat(option.get("deptName")).isEqualTo("信息技术部");
        });

        ResponseEntity<Map> dashboardResponse = restTemplate.exchange(
                url("/api/reports/dashboard"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );
        Map<String, Object> dashboard = data(dashboardResponse);
        assertThat(numberValue(dashboard.get("todayCreatedCount")).longValue()).isGreaterThanOrEqualTo(1);
        assertThat(numberValue(dashboard.get("processingCount")).longValue()).isGreaterThanOrEqualTo(1);
        assertThat(numberValue(dashboard.get("overdueCount")).longValue()).isGreaterThanOrEqualTo(0);

        ResponseEntity<Map> distributionResponse = restTemplate.exchange(
                url("/api/reports/category-distribution"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );
        List<Map<String, Object>> distribution = data(distributionResponse);
        assertThat(distribution).anySatisfy(item -> {
            assertThat(item.get("categoryName")).isEqualTo("网络故障");
            assertThat(numberValue(item.get("ticketCount")).longValue()).isGreaterThanOrEqualTo(1);
        });
    }

    @Test
    void 规则读写权限分离并支持启停规则() {
        String managerToken = loginAndToken("ops_manager", "123456");

        ResponseEntity<Map> listResponse = restTemplate.exchange(
                url("/api/rules/dispatch"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(managerToken)),
                Map.class
        );
        assertThat(listResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(listResponse.getBody()).isNotNull();
        assertThat(listResponse.getBody().get("code")).isEqualTo(0);

        ResponseEntity<Map> managerCreateResponse = postRaw("/api/rules/dispatch", Map.of(
                "categoryId", 1,
                "deptId", 2,
                "skillCode", "NETWORK",
                "assigneeId", 3,
                "priority", 99
        ), managerToken);
        assertThat(managerCreateResponse.getBody()).isNotNull();
        assertThat(managerCreateResponse.getBody().get("code")).isEqualTo(403);

        Map<String, Object> disabled = putData("/api/rules/dispatch/1/enabled", Map.of("enabled", 0));
        assertThat(disabled.get("enabled")).isEqualTo(0);

        Map<String, Object> enabled = putData("/api/rules/dispatch/1/enabled", Map.of("enabled", 1));
        assertThat(enabled.get("enabled")).isEqualTo(1);
    }

    @Test
    void 用户支持启停和重置密码() {
        Map<String, Object> user = postData("/api/system/users", Map.of(
                "username", "reset_case",
                "password", "123456",
                "realName", "重置密码用户",
                "phone", "13900000000",
                "email", "reset@ticketflow.local",
                "deptId", 2,
                "roleIds", List.of(4)
        ));
        Long userId = numberValue(user.get("id")).longValue();

        Map<String, Object> disabled = putData("/api/system/users/" + userId + "/status", Map.of("status", "DISABLED"));
        assertThat(disabled.get("status")).isEqualTo("DISABLED");
        ResponseEntity<Map> disabledLoginResponse = loginRaw("reset_case", "123456");
        assertThat(disabledLoginResponse.getBody()).isNotNull();
        assertThat(disabledLoginResponse.getBody().get("code")).isEqualTo(401);

        Map<String, Object> enabled = putData("/api/system/users/" + userId + "/status", Map.of("status", "ENABLED"));
        assertThat(enabled.get("status")).isEqualTo("ENABLED");
        putData("/api/system/users/" + userId + "/reset-password", Map.of("password", "654321"));

        ResponseEntity<Map> oldPasswordResponse = loginRaw("reset_case", "123456");
        assertThat(oldPasswordResponse.getBody()).isNotNull();
        assertThat(oldPasswordResponse.getBody().get("code")).isEqualTo(401);
        assertThat(loginAndToken("reset_case", "654321")).isNotBlank();
    }

    @Test
    @SuppressWarnings("unchecked")
    void 附件可以上传并按业务对象查询() {
        Map<String, Object> ticket = postData("/api/tickets", Map.of(
                "title", "附件集成测试工单",
                "description", "验证附件业务权限",
                "categoryId", 1,
                "priority", "LOW"
        ));
        Long ticketId = numberValue(ticket.get("id")).longValue();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("集成测试日志内容".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "integration.log";
            }
        });

        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<Map> uploadResponse = restTemplate.postForEntity(
                url("/api/attachments/upload?businessType=TICKET&businessId=" + ticketId),
                new HttpEntity<>(body, headers),
                Map.class
        );

        assertThat(uploadResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> uploadData = data(uploadResponse);
        assertThat(uploadData.get("originalName")).isEqualTo("integration.log");

        ResponseEntity<Map> listResponse = restTemplate.exchange(
                url("/api/attachments?businessType=TICKET&businessId=" + ticketId),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) data(listResponse);
        assertThat(attachments).anySatisfy(attachment -> assertThat(attachment.get("originalName")).isEqualTo("integration.log"));

        String employeeToken = loginAndToken("employee", "123456");
        ResponseEntity<Map> forbiddenListResponse = restTemplate.exchange(
                url("/api/attachments?businessType=TICKET&businessId=" + ticketId),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(employeeToken)),
                Map.class
        );
        assertThat(forbiddenListResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(forbiddenListResponse.getBody()).isNotNull();
        assertThat(forbiddenListResponse.getBody().get("code")).isEqualTo(403);
    }

    @Test
    void RabbitMQ通知事件可以异步生成站内信并支持幂等已读() throws Exception {
        long unreadBefore = unreadCount();
        notificationPublisher.publish(new NotificationEvent(
                1L,
                "集成测试通知",
                "RabbitMQ 通知链路验证",
                "INTEGRATION_TEST",
                90001L
        ));
        notificationPublisher.publish(new NotificationEvent(
                1L,
                "集成测试通知",
                "RabbitMQ 通知链路验证",
                "INTEGRATION_TEST",
                90001L
        ));

        Map<String, Object> message = waitForMessage("INTEGRATION_TEST", 90001L);

        assertThat(message).isNotNull();
        assertThat(message.get("title")).isEqualTo("集成测试通知");
        assertThat(countMessages("INTEGRATION_TEST", 90001L)).isEqualTo(1);
        assertThat(unreadCount()).isGreaterThanOrEqualTo(unreadBefore + 1);

        ResponseEntity<Map> readResponse = restTemplate.postForEntity(
                url("/api/messages/" + numberValue(message.get("id")).longValue() + "/read"),
                new HttpEntity<>(Map.of(), authHeaders()),
                Map.class
        );
        assertThat(readResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(readResponse.getBody()).isNotNull();
        assertThat(readResponse.getBody().get("code")).isEqualTo(0);
        assertThat(unreadCount()).isEqualTo(unreadBefore);
    }

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

    @Test
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

        Map<String, Object> assignedMessage = waitForMessage("TICKET_ASSIGNED", ticketId, engineerToken);
        assertThat(assignedMessage).isNotNull();
        assertThat(assignedMessage.get("title")).isEqualTo("你有新的待接单工单");

        postData("/api/tickets/" + ticketId + "/accept", Map.of(), engineerToken);
        postData("/api/tickets/" + ticketId + "/process", Map.of(
                "result", "第三阶段处理完成"
        ), engineerToken);

        Map<String, Object> processMessage = waitForMessage("TICKET_PENDING_CONFIRM", ticketId, employeeToken);
        assertThat(processMessage).isNotNull();
        assertThat(processMessage.get("title")).isEqualTo("工单等待确认");

        postData("/api/tickets/" + ticketId + "/comments", Map.of(
                "content", "第三阶段补充评论",
                "internalOnly", false
        ), employeeToken);
        Map<String, Object> commentMessage = waitForMessage("TICKET_COMMENTED", ticketId, engineerToken);
        assertThat(commentMessage).isNotNull();
    }

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

    @SuppressWarnings("unchecked")
    private String loginAndToken() {
        return loginAndToken("admin", "123456");
    }

    @SuppressWarnings("unchecked")
    private String loginAndToken(String username, String password) {
        ResponseEntity<Map> loginResponse = loginRaw(username, password);

        assertThat(loginResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> loginData = data(loginResponse);
        String loginToken = (String) loginData.get("token");
        assertThat(loginToken).isNotBlank();
        return loginToken;
    }

    private ResponseEntity<Map> loginRaw(String username, String password) {
        return restTemplate.postForEntity(
                url("/api/auth/login"),
                new LoginRequest(username, password),
                Map.class
        );
    }

    private HttpHeaders authHeaders() {
        return authHeaders(token);
    }

    private HttpHeaders authHeaders(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postData(String path, Object body) {
        return postData(path, body, token);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postData(String path, Object body, String bearerToken) {
        ResponseEntity<Map> response = postRaw(path, body, bearerToken);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return data(response);
    }

    private ResponseEntity<Map> postRaw(String path, Object body, String bearerToken) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url(path),
                new HttpEntity<>(body, authHeaders(bearerToken)),
                Map.class
        );
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> putData(String path, Object body) {
        ResponseEntity<Map> response = restTemplate.exchange(
                url(path),
                HttpMethod.PUT,
                new HttpEntity<>(body, authHeaders()),
                Map.class
        );
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return data(response);
    }

    @SuppressWarnings("unchecked")
    private <T> T data(ResponseEntity<Map> response) {
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("code")).isEqualTo(0);
        return (T) body.get("data");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> waitForMessage(String businessType, Long businessId) throws Exception {
        return waitForMessage(businessType, businessId, token);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> waitForMessage(String businessType, Long businessId, String bearerToken) throws Exception {
        for (int i = 0; i < 20; i++) {
            List<Map<String, Object>> records = messageRecords(bearerToken);
            Map<String, Object> message = records.stream()
                    .filter(record -> businessType.equals(record.get("businessType"))
                            && businessId.equals(numberValue(record.get("businessId")).longValue()))
                    .findFirst()
                    .orElse(null);
            if (message != null) {
                return message;
            }
            Thread.sleep(200L);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> messageRecords() {
        return messageRecords(token);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> messageRecords(String bearerToken) {
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/messages?pageNo=1&pageSize=20"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(bearerToken)),
                Map.class
        );
        Map<String, Object> page = data(response);
        return (List<Map<String, Object>>) page.get("records");
    }

    private long countMessages(String businessType, Long businessId) {
        return messageRecords().stream()
                .filter(record -> businessType.equals(record.get("businessType"))
                        && businessId.equals(numberValue(record.get("businessId")).longValue()))
                .count();
    }

    @SuppressWarnings("unchecked")
    private long unreadCount() {
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/messages/unread-count"),
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                Map.class
        );
        Map<String, Object> data = data(response);
        return numberValue(data.get("count")).longValue();
    }

    private Number numberValue(Object value) {
        assertThat(value).isInstanceOf(Number.class);
        return (Number) value;
    }

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }
}
