package com.ticketflow.system.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户密码重置请求 DTO。
 *
 * @param password 新登录密码。
 */
public record UserPasswordResetRequest(
        @NotBlank(message = "新密码不能为空")
        String password
) {
}
