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
 * @param level 消息级别，例如 INFO、WARNING、ERROR。
 * @param dedupeKey 幂等键，用于排查重复消息来源。
 * @param readFlag 是否已读：1 已读，0 未读。
 * @param readAt 已读时间，未读时为空。
 * @param createdAt 创建时间。
 */
public record NotificationMessageResponse(
        Long id,
        Long receiverId,
        String title,
        String content,
        String businessType,
        Long businessId,
        String level,
        String dedupeKey,
        Integer readFlag,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
