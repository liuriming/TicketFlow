package com.ticketflow.audit.dto;

import java.time.LocalDateTime;

/**
 * 操作审计日志响应 DTO。
 *
 * @param id 日志 ID。
 * @param operatorId 操作人用户 ID。
 * @param operatorName 操作人展示名称。
 * @param requestMethod HTTP 请求方法。
 * @param requestUri 请求路径。
 * @param queryString 查询字符串。
 * @param clientIp 客户端 IP。
 * @param success 是否执行成功：1 成功，0 失败。
 * @param errorMessage 异常消息。
 * @param durationMs 请求处理耗时，单位毫秒。
 * @param createdAt 操作时间。
 */
public record OperationLogResponse(
        Long id,
        Long operatorId,
        String operatorName,
        String requestMethod,
        String requestUri,
        String queryString,
        String clientIp,
        Integer success,
        String errorMessage,
        Long durationMs,
        LocalDateTime createdAt
) {
}
