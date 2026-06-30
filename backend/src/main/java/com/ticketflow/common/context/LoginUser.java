package com.ticketflow.common.context;

import com.ticketflow.system.enums.DataScopeType;

import java.util.List;

/**
 * 登录用户快照。
 *
 * @param userId 用户 ID，用于业务数据归属和审计。
 * @param username 登录账号，用于展示和日志定位。
 * @param realName 用户真实姓名，用于工单处理人、创建人等界面展示。
 * @param deptId 所属部门 ID，用于部门数据权限判断。
 * @param deptPath 部门路径，格式如 1/2/5，用于判断上下级部门关系。
 * @param dataScope 数据范围，控制用户可访问的业务数据集合。
 * @param roles 当前用户拥有的角色编码集合。
 * @param permissions 当前用户拥有的菜单权限标识集合。
 */
public record LoginUser(
        Long userId,
        String username,
        String realName,
        Long deptId,
        String deptPath,
        DataScopeType dataScope,
        List<String> roles,
        List<String> permissions
) {
}
