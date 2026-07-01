INSERT IGNORE INTO sys_dept (id, parent_id, dept_name, path, sort_order, enabled)
VALUES
    (1, 0, '总部', '1', 1, 1),
    (2, 1, '信息技术部', '1/2', 2, 1),
    (3, 1, '行政部', '1/3', 3, 1);

INSERT IGNORE INTO sys_role (id, role_name, role_code, data_scope, sort_order, enabled)
VALUES
    (1, '系统管理员', 'ADMIN', 'ALL', 1, 1),
    (2, '运维主管', 'OPS_MANAGER', 'DEPT_AND_CHILD', 2, 1),
    (3, '运维工程师', 'OPS_ENGINEER', 'DEPT', 3, 1),
    (4, '员工', 'EMPLOYEE', 'SELF', 4, 1);

INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, type, path, component, icon, permission, sort_order, visible)
VALUES
    (1, 0, '工作台', 'MENU', '/dashboard', 'dashboard/index', 'Monitor', 'dashboard:view', 1, 1),
    (2, 0, '工单管理', 'CATALOG', '/tickets', 'layout/router-view', 'Tickets', 'ticket:view', 2, 1),
    (3, 2, '工单列表', 'MENU', '/tickets/list', 'ticket/list', 'Document', 'ticket:list', 1, 1),
    (4, 2, '工单分类', 'MENU', '/tickets/categories', 'ticket/category', 'Folder', 'ticket:category:list', 2, 1),
    (5, 0, '规则配置', 'CATALOG', '/rules', 'layout/router-view', 'SetUp', 'rule:view', 3, 1),
    (6, 5, '派单规则', 'MENU', '/rules/dispatch', 'rule/dispatch', 'Connection', 'rule:dispatch:list', 1, 1),
    (7, 5, 'SLA 规则', 'MENU', '/rules/sla', 'rule/sla', 'Timer', 'rule:sla:list', 2, 1),
    (8, 0, '系统管理', 'CATALOG', '/system', 'layout/router-view', 'Setting', 'system:view', 4, 1),
    (9, 8, '用户管理', 'MENU', '/system/users', 'system/user', 'User', 'system:user:list', 1, 1),
    (10, 8, '角色管理', 'MENU', '/system/roles', 'system/role', 'Avatar', 'system:role:list', 2, 1),
    (11, 8, '菜单管理', 'MENU', '/system/menus', 'system/menu', 'Menu', 'system:menu:list', 3, 1),
    (12, 8, '部门管理', 'MENU', '/system/depts', 'system/dept', 'OfficeBuilding', 'system:dept:list', 4, 1),
    (13, 0, '统计看板', 'MENU', '/reports', 'report/index', 'TrendCharts', 'report:view', 5, 1),
    (14, 3, '转派工单', 'BUTTON', NULL, NULL, NULL, 'ticket:transfer', 10, 1),
    (15, 3, '创建工单', 'BUTTON', NULL, NULL, NULL, 'ticket:create', 11, 1),
    (16, 3, '接单', 'BUTTON', NULL, NULL, NULL, 'ticket:accept', 12, 1),
    (17, 3, '处理工单', 'BUTTON', NULL, NULL, NULL, 'ticket:process', 13, 1),
    (18, 3, '确认关闭', 'BUTTON', NULL, NULL, NULL, 'ticket:confirm-close', 14, 1),
    (19, 3, '驳回处理结果', 'BUTTON', NULL, NULL, NULL, 'ticket:reject', 15, 1),
    (20, 3, '取消工单', 'BUTTON', NULL, NULL, NULL, 'ticket:cancel', 16, 1),
    (21, 3, '评论工单', 'BUTTON', NULL, NULL, NULL, 'ticket:comment', 17, 1),
    (22, 3, '上传工单附件', 'BUTTON', NULL, NULL, NULL, 'ticket:attachment:upload', 18, 1),
    (23, 9, '维护用户', 'BUTTON', NULL, NULL, NULL, 'system:user:write', 10, 1),
    (24, 10, '维护角色', 'BUTTON', NULL, NULL, NULL, 'system:role:write', 10, 1),
    (25, 11, '维护菜单', 'BUTTON', NULL, NULL, NULL, 'system:menu:write', 10, 1),
    (26, 12, '维护部门', 'BUTTON', NULL, NULL, NULL, 'system:dept:write', 10, 1),
    (27, 4, '维护工单分类', 'BUTTON', NULL, NULL, NULL, 'ticket:category:write', 10, 1),
    (28, 6, '维护派单规则', 'BUTTON', NULL, NULL, NULL, 'rule:dispatch:write', 10, 1),
    (29, 7, '维护 SLA 规则', 'BUTTON', NULL, NULL, NULL, 'rule:sla:write', 10, 1),
    (30, 0, '消息中心', 'MENU', '/messages', 'message/index', 'Bell', 'message:view', 6, 1),
    (31, 8, '审计日志', 'MENU', '/system/audit-logs', 'system/audit-log', 'DocumentChecked', 'audit:log:list', 5, 1);

UPDATE sys_menu SET visible = 1 WHERE type = 'BUTTON' AND id BETWEEN 14 AND 29;

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT menu_id, 1, menu_id FROM (
    SELECT 1 AS menu_id UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL
    SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL
    SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL
    SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL
    SELECT 24 UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL
    SELECT 30 UNION ALL SELECT 31
) t;

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
VALUES
    (101, 4, 1),
    (102, 4, 2),
    (103, 4, 3),
    (104, 4, 15),
    (105, 4, 18),
    (106, 4, 19),
    (107, 4, 20),
    (108, 4, 21),
    (109, 4, 22),
    (110, 4, 30),
    (201, 3, 1),
    (202, 3, 2),
    (203, 3, 3),
    (204, 3, 13),
    (205, 3, 16),
    (206, 3, 17),
    (207, 3, 21),
    (208, 3, 22),
    (209, 3, 30),
    (301, 2, 1),
    (302, 2, 2),
    (303, 2, 3),
    (304, 2, 4),
    (305, 2, 5),
    (306, 2, 6),
    (307, 2, 7),
    (308, 2, 13),
    (309, 2, 14),
    (310, 2, 16),
    (311, 2, 17),
    (312, 2, 18),
    (313, 2, 19),
    (314, 2, 20),
    (315, 2, 21),
    (316, 2, 22),
    (317, 2, 30);

INSERT IGNORE INTO sys_user (id, username, password_hash, password_salt, real_name, phone, email, dept_id, status)
VALUES
    (1, 'admin', '9f3e7d6f2282e9db1fe242b9a1f8862e6c3ff60ceb2d991e8374275397c3e74f', 'ticketflow-admin', '系统管理员', '13800000000', 'admin@ticketflow.local', 2, 'ENABLED'),
    (2, 'employee', 'f6362811435149beb76dcfc85adb03373881f0105d85bd5fd9e9879c2eb7efab', 'ticketflow-employee', '报修员工', '13800000001', 'employee@ticketflow.local', 3, 'ENABLED'),
    (3, 'ops_engineer', '24368856361facfec7b584d6fc81b86d970690fdd4f3ab01739a2edeb2d69de9', 'ticketflow-engineer', '运维工程师', '13800000002', 'engineer@ticketflow.local', 2, 'ENABLED'),
    (4, 'ops_manager', '6c99f6ee5e2c688eb91af45696e0ef06cc7d1bfc9e7c30de4099f634c4ad944d', 'ticketflow-manager', '运维主管', '13800000003', 'manager@ticketflow.local', 2, 'ENABLED');

INSERT IGNORE INTO sys_user_role (id, user_id, role_id)
VALUES
    (1, 1, 1),
    (2, 2, 4),
    (3, 3, 3),
    (4, 4, 2);

INSERT IGNORE INTO ticket_category (id, parent_id, category_name, category_code, sort_order, enabled)
VALUES
    (1, 0, '网络故障', 'NETWORK', 1, 1),
    (2, 0, '账号权限', 'ACCOUNT', 2, 1),
    (3, 0, '服务器问题', 'SERVER', 3, 1);

INSERT IGNORE INTO sla_rule (id, priority, response_minutes, resolve_minutes, enabled)
VALUES
    (1, 'LOW', 240, 2880, 1),
    (2, 'MEDIUM', 120, 1440, 1),
    (3, 'HIGH', 60, 480, 1),
    (4, 'URGENT', 30, 240, 1);

INSERT IGNORE INTO dispatch_rule (id, category_id, dept_id, skill_code, assignee_id, priority, enabled)
VALUES
    (1, 1, NULL, 'NETWORK', 3, 90, 1),
    (2, 2, NULL, 'ACCOUNT', 3, 80, 1),
    (3, 3, NULL, 'SERVER', 3, 70, 1);
