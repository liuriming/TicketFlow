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
 * @param level 消息级别，例如 INFO、WARNING、ERROR，用于消息中心视觉区分。
 * @param dedupeKey 幂等键，同一接收人下相同幂等键只保存一条消息。
 */
public record NotificationEvent(
        Long receiverId,
        String title,
        String content,
        String businessType,
        Long businessId,
        String level,
        String dedupeKey
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public NotificationEvent {
        if (!hasText(level)) {
            level = "INFO";
        }
        if (!hasText(dedupeKey)) {
            dedupeKey = businessType + ":" + businessId;
        }
    }

    /**
     * 兼容旧通知事件创建方式，默认使用 INFO 级别，并以业务类型和业务 ID 作为幂等键。
     *
     * @param receiverId 接收人用户 ID。
     * @param title 消息标题。
     * @param content 消息内容。
     * @param businessType 业务类型。
     * @param businessId 业务 ID。
     */
    public NotificationEvent(
            Long receiverId,
            String title,
            String content,
            String businessType,
            Long businessId
    ) {
        this(receiverId, title, content, businessType, businessId, "INFO", businessType + ":" + businessId);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
