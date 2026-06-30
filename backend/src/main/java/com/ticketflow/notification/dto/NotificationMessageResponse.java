package com.ticketflow.notification.dto;

import java.time.LocalDateTime;

/**
 * 通知消息响应 DTO。
 *
 * @param id 消息 ID。
 * @param receiverId 接收人用户 ID。
 * @param title 消息标题。
 * @param content 消息内容。
 * @param businessType 业务类型。
 * @param businessId 业务 ID。
 * @param readFlag 是否已读：1 已读，0 未读。
 * @param createdAt 创建时间。
 */
public record NotificationMessageResponse(
        Long id,
        Long receiverId,
        String title,
        String content,
        String businessType,
        Long businessId,
        Integer readFlag,
        LocalDateTime createdAt
) {
}
