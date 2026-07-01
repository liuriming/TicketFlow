package com.ticketflow.sla.enums;

/**
 * SLA 告警类型。
 *
 * <p>用于区分响应临期、响应超时、处理临期和处理超时四类提醒，配合告警记录表实现
 * 同一工单同一告警类型只通知一次。</p>
 */
public enum SlaAlertType {

    /**
     * 响应截止时间临近。
     */
    RESPONSE_WARNING,

    /**
     * 响应截止时间已超时。
     */
    RESPONSE_OVERDUE,

    /**
     * 处理截止时间临近。
     */
    RESOLVE_WARNING,

    /**
     * 处理截止时间已超时。
     */
    RESOLVE_OVERDUE
}
