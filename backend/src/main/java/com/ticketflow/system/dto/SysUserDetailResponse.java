package com.ticketflow.system.dto;

import com.ticketflow.system.enums.UserStatus;

import java.util.List;

/**
 * 用户详情响应 DTO。
 *
 * <p>用于用户管理页面展示和编辑账号资料。响应中不包含密码摘要和盐值，只返回前端需要展示、
 * 回填和授权编辑的字段。</p>
 *
 * @param id 用户 ID。
 * @param username 登录账号。
 * @param realName 用户真实姓名。
 * @param phone 手机号。
 * @param email 邮箱。
 * @param deptId 所属部门 ID。
 * @param status 用户状态。
 * @param roleIds 已绑定角色 ID 集合。
 */
public record SysUserDetailResponse(
        Long id,
        String username,
        String realName,
        String phone,
        String email,
        Long deptId,
        UserStatus status,
        List<Long> roleIds
) {
}
