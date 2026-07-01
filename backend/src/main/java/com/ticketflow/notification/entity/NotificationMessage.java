package com.ticketflow.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知消息实体。
 *
 * <p>用于站内信列表、未读数量、工单协同通知和 SLA 提醒落库。
 * RabbitMQ 负责异步投递，数据库保存最终消息状态和幂等键。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_message")
public class NotificationMessage extends BaseEntity {

    /**
     * 接收人用户 ID。
     */
    private Long receiverId;

    /**
     * 消息标题。
     */
    private String title;

    /**
     * 消息内容。
     */
    private String content;

    /**
     * 业务类型，例如 TICKET_SLA_OVERDUE。
     */
    private String businessType;

    /**
     * 业务 ID。
     */
    private Long businessId;

    /**
     * 消息级别，例如 INFO、WARNING、ERROR，用于前端消息中心区分展示。
     */
    private String level;

    /**
     * 幂等键，同一接收人下相同幂等键只允许保存一条消息。
     */
    private String dedupeKey;

    /**
     * 是否已读：1 已读，0 未读。
     */
    private Integer readFlag;

    /**
     * 已读时间，未读消息为空。
     */
    private LocalDateTime readAt;
}
