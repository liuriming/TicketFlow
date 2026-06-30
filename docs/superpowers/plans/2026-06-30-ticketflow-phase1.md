# TicketFlow 第一阶段 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立 TicketFlow 第一阶段可运行骨架，完成登录认证、RBAC、部门和用户管理，并保留后续工单闭环模块边界。

**Architecture:** 后端采用 Spring Boot 3 模块化单体，按业务包分层；前端采用 Vue3 单页管理后台；基础设施通过 Docker Compose 提供 MySQL、Redis、RabbitMQ 和 MinIO。

**Tech Stack:** Java 17、Spring Boot 3、MyBatis-Plus、MySQL、Redis、RabbitMQ、MinIO、Vue3、Element Plus、Docker Compose。

---

### Task 1: 后端基础和领域测试

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/test/java/com/ticketflow/system/domain/DataScopeMatcherTest.java`
- Create: `backend/src/test/java/com/ticketflow/ticket/domain/TicketStateMachineTest.java`
- Create: `backend/src/test/java/com/ticketflow/sla/domain/SlaCalculatorTest.java`
- Create: `backend/src/test/java/com/ticketflow/rule/domain/DispatchEngineTest.java`

- [x] **Step 1: 写领域测试**

覆盖数据权限、工单状态流转、SLA 截止时间、自动派单。

- [x] **Step 2: 运行测试并确认失败**

Run: `& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' test`

Expected: 因领域类不存在而编译失败。

### Task 2: 后端系统权限模块

**Files:**
- Create: `backend/src/main/java/com/ticketflow/system/**`
- Create: `backend/src/main/resources/schema.sql`
- Create: `backend/src/main/resources/data.sql`

- [x] **Step 1: 创建实体、DTO、Mapper、Service、Controller**

实现登录、退出、当前用户、菜单路由、用户、角色、菜单、部门管理基础接口。

- [ ] **Step 2: 运行后端测试**

Run: `& 'D:\tools\maven\apache-maven-3.9.16\apache-maven-3.9.16\bin\mvn.cmd' test`

Expected: 领域测试全部通过。

### Task 3: 前端管理后台

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/src/**`

- [ ] **Step 1: 创建 Vue3 + Element Plus 后台骨架**

实现登录页、后台布局、工作台、工单、规则、系统、报表页面骨架。

- [ ] **Step 2: 运行前端构建**

Run: `npm install && npm run build`

Expected: 构建成功生成 `frontend/dist`。

### Task 4: 部署和文档

**Files:**
- Create: `docker-compose.yml`
- Create: `README.md`

- [x] **Step 1: 添加基础设施编排**

包含 MySQL、Redis、RabbitMQ、MinIO。

- [ ] **Step 2: 汇总验证结果**

记录后端测试和前端构建结果，并保留未完成的后续阶段任务。
