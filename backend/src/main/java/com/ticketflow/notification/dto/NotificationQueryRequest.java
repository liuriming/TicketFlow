package com.ticketflow.notification.dto;

/**
 * 消息中心查询 DTO。
 *
 * @param readFlag 已读状态，0 表示未读，1 表示已读，空值表示全部。
 * @param businessType 业务类型，例如 TICKET_ASSIGNED、SLA_RESOLVE_OVERDUE。
 * @param keyword 标题或内容关键字，用于消息中心快速检索。
 */
public record NotificationQueryRequest(
        Integer readFlag,
        String businessType,
        String keyword
) {
}
