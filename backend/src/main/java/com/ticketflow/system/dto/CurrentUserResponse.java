package com.ticketflow.system.dto;

import com.ticketflow.system.enums.DataScopeType;

import java.util.List;

/**
 * 当前登录用户响应 DTO。
 *
 * @param userId 用户 ID。
 * @param username 登录账号。
 * @param realName 用户真实姓名。
 * @param deptId 所属部门 ID。
 * @param deptName 所属部门名称。
 * @param dataScope 数据权限范围。
 * @param roles 角色编码集合。
 * @param permissions 权限标识集合。
 */
public record CurrentUserResponse(
        Long userId,
        String username,
        String realName,
        Long deptId,
        String deptName,
        DataScopeType dataScope,
        List<String> roles,
        List<String> permissions
) {
}
