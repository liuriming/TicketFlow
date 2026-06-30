package com.ticketflow.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 用户保存请求 DTO。
 *
 * @param username 登录账号，新建时必填。
 * @param password 登录密码，新建时必填，更新时为空表示不修改密码。
 * @param realName 用户真实姓名。
 * @param phone 手机号。
 * @param email 邮箱。
 * @param deptId 所属部门 ID。
 * @param roleIds 绑定的角色 ID 集合。
 */
public record SysUserSaveRequest(
        @NotBlank(message = "登录账号不能为空")
        String username,
        String password,
        @NotBlank(message = "用户姓名不能为空")
        String realName,
        String phone,
        String email,
        @NotNull(message = "所属部门不能为空")
        Long deptId,
        List<Long> roleIds
) {
}
