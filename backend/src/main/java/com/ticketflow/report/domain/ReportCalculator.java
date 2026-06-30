package com.ticketflow.report.domain;

import com.ticketflow.ticket.entity.Ticket;

import java.time.Duration;
import java.util.List;

/**
 * 报表计算器。
 *
 * <p>负责根据工单集合计算总量、超时率和平均处理时长等纯统计指标，不依赖数据库。</p>
 */
public final class ReportCalculator {

    private ReportCalculator() {
    }

    /**
     * 计算工单概览指标。
     *
     * @param tickets 工单集合。
     * @return 报表概览。
     */
    public static ReportOverview overview(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            return new ReportOverview(0, 0, 0);
        }
        long overdueCount = tickets.stream()
                .filter(ticket -> ticket.getClosedAt() != null
                        && ticket.getResolveDeadline() != null
                        && ticket.getClosedAt().isAfter(ticket.getResolveDeadline()))
                .count();
        double averageResolveHours = tickets.stream()
                .filter(ticket -> ticket.getCreatedAt() != null && ticket.getClosedAt() != null)
                .mapToDouble(ticket -> Duration.between(ticket.getCreatedAt(), ticket.getClosedAt()).toMinutes() / 60.0)
                .average()
                .orElse(0);
        return new ReportOverview(
                tickets.size(),
                roundOneDecimal(overdueCount * 100.0 / tickets.size()),
                roundOneDecimal(averageResolveHours)
        );
    }

    private static double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
