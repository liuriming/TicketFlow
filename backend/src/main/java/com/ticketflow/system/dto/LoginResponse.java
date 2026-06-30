package com.ticketflow.system.dto;

/**
 * 登录响应 DTO。
 *
 * @param token 访问令牌，前端后续请求通过 Authorization 请求头携带。
 * @param expiresInSeconds token 剩余有效秒数。
 */
public record LoginResponse(String token, long expiresInSeconds) {
}
