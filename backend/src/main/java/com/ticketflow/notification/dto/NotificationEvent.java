package com.ticketflow.notification.dto;

import java.io.Serializable;

/**
 * 通知事件 DTO。
 *
 * @param receiverId 接收人用户 ID。
 * @param title 消息标题。
 * @param content 消息内容。
 * @param businessType 业务类型，例如 SLA_RESOLVE_OVERDUE。
 * @param businessId 业务 ID。
 */
public record NotificationEvent(
        Long receiverId,
        String title,
        String content,
        String businessType,
        Long businessId
) implements Serializable {

    private static final long serialVersionUID = 1L;
}
