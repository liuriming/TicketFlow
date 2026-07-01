package com.ticketflow.sla.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import com.ticketflow.sla.enums.SlaAlertType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * SLA 告警记录实体。
 *
 * <p>记录某个工单的某类 SLA 告警是否已经发布，避免定时任务每分钟扫描时重复推送
 * 同一条临期或超时消息。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sla_alert_record")
public class SlaAlertRecord extends BaseEntity {

    /**
     * 工单 ID。
     */
    private Long ticketId;

    /**
     * 告警类型，区分响应临期、响应超时、处理临期和处理超时。
     */
    private SlaAlertType alertType;

    /**
     * 首次发布告警的时间。
     */
    private LocalDateTime publishedAt;
}
