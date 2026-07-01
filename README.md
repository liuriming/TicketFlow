# TicketFlow 企业工单协作系统

TicketFlow 是一个面向企业内部 IT 运维报修的 Java 全栈项目，采用“模块化单体 + 前后端分离”方式落地。

## 技术栈

- 后端：Java 17、Spring Boot 3、MyBatis-Plus、MySQL、Redis、RabbitMQ、MinIO
- 前端：Vue3、Element Plus、Pinia、Vue Router、Axios
- 部署：Docker Compose
- Maven：`D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd`

## 当前进展

当前已经建立项目骨架，并覆盖以下能力：

- 登录、退出、当前用户信息、菜单路由接口
- 用户、角色、菜单、部门管理接口
- RBAC 角色、菜单、数据范围模型
- 工单创建、查询、详情、接单、处理、转派、确认关闭、驳回、取消
- 工单评论和工单流转记录，流转记录可作为工单操作日志
- 附件上传、下载、删除和绑定业务对象，文件存储到 MinIO，数据库保存元数据
- 工单分类、派单规则、SLA 规则配置接口
- SLA 超时扫描，RabbitMQ 异步通知，站内信列表、已读、未读数量
- 消息中心支持筛选、批量已读和工单关联跳转
- 工单关键动作会通过 RabbitMQ 异步生成站内信
- SLA 支持临期预警、超时提醒和重复告警控制
- 操作审计记录变更类接口的操作人、请求地址、结果和耗时
- 统计看板接口：工单数量、超时率、平均处理时长、人员工作量
- 工单状态机、自动派单、SLA 计算、数据权限判断、附件对象名、报表计算单元测试
- Testcontainers 登录鉴权集成测试，Docker daemon 可用时会启动 MySQL、Redis、RabbitMQ
- MySQL、Redis、RabbitMQ、MinIO 的 Docker Compose 环境

## 本地启动

启动基础服务：

```powershell
docker compose up -d
```

如果只想启动数据库、中间件，不启动前后端镜像，可以执行：

```powershell
docker compose up -d mysql redis rabbitmq minio
```

运行后端测试：

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' test
```

启动后端：

```powershell
cd backend
& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' spring-boot:run
```

默认管理员账号：

- 账号：`admin`
- 密码：`123456`

接口文档地址：

- `http://localhost:8080/swagger-ui.html`

## Docker Compose 部署

项目已经提供前端、后端和基础设施的 Compose 编排：

```powershell
docker compose up -d --build
```

启动后访问：

- 前端管理后台：`http://localhost:3000`
- 后端接口文档：`http://localhost:8080/swagger-ui.html`
- RabbitMQ 管理台：`http://localhost:15672`
- MinIO 控制台：`http://localhost:9001`

如果 `mvn test` 中 Testcontainers 集成测试被跳过，请先确认 Docker daemon 是否可用：

```powershell
docker info
```

当前集成测试使用 `@Testcontainers(disabledWithoutDocker = true)`，Docker 可用时会自动执行登录鉴权集成测试。
