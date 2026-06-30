package com.ticketflow.system.dto;

/**
 * 用户下拉选项响应 DTO。
 *
 * @param id 用户 ID。
 * @param username 登录账号。
 * @param realName 用户姓名。
 * @param deptId 所属部门 ID。
 * @param deptName 所属部门名称。
 */
public record UserOptionResponse(
        Long id,
        String username,
        String realName,
        Long deptId,
        String deptName
) {
}
