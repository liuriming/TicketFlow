package com.ticketflow.sla.domain;

import java.time.LocalDateTime;

/**
 * SLA 计算器。
 *
 * <p>根据工单创建时间和 SLA 规则分钟数计算响应、处理截止时间，并判断是否超时。</p>
 */
public final class SlaCalculator {

    private SlaCalculator() {
    }

    /**
     * 计算 SLA 截止时间。
     *
     * @param createdAt 工单创建时间。
     * @param responseMinutes 响应时限分钟数。
     * @param resolveMinutes 处理时限分钟数。
     * @return SLA 截止时间。
     */
    public static SlaDeadline calculate(LocalDateTime createdAt, int responseMinutes, int resolveMinutes) {
        return new SlaDeadline(createdAt.plusMinutes(responseMinutes), createdAt.plusMinutes(resolveMinutes));
    }

    /**
     * 判断是否响应超时。
     *
     * @param deadline SLA 截止时间。
     * @param now 当前时间。
     * @return true 表示响应已超时。
     */
    public static boolean isResponseOverdue(SlaDeadline deadline, LocalDateTime now) {
        return now.isAfter(deadline.responseDeadline());
    }

    /**
     * 判断是否处理超时。
     *
     * @param deadline SLA 截止时间。
     * @param now 当前时间。
     * @return true 表示处理已超时。
     */
    public static boolean isResolveOverdue(SlaDeadline deadline, LocalDateTime now) {
        return now.isAfter(deadline.resolveDeadline());
    }
}
