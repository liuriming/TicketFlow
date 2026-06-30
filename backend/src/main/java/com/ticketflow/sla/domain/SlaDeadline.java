package com.ticketflow.sla.domain;

import java.time.LocalDateTime;

/**
 * SLA 截止时间。
 *
 * @param responseDeadline 响应截止时间。
 * @param resolveDeadline 处理截止时间。
 */
public record SlaDeadline(LocalDateTime responseDeadline, LocalDateTime resolveDeadline) {
}
