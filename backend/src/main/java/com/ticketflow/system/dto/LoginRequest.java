package com.ticketflow.system.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO。
 *
 * @param username 登录账号，不能为空。
 * @param password 登录密码，不能为空，服务端只用于校验，不会持久化明文。
 */
public record LoginRequest(
        @NotBlank(message = "登录账号不能为空")
        String username,

        @NotBlank(message = "登录密码不能为空")
        String password
) {
}
