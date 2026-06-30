package com.ticketflow.report.domain;

import com.ticketflow.ticket.entity.Ticket;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportCalculatorTest {

    @Test
    void 根据工单集合计算总数超时率和平均处理小时数() {
        Ticket normal = new Ticket();
        normal.setCreatedAt(LocalDateTime.of(2026, 6, 30, 9, 0));
        normal.setClosedAt(LocalDateTime.of(2026, 6, 30, 11, 0));
        normal.setResolveDeadline(LocalDateTime.of(2026, 6, 30, 12, 0));

        Ticket overdue = new Ticket();
        overdue.setCreatedAt(LocalDateTime.of(2026, 6, 30, 9, 0));
        overdue.setClosedAt(LocalDateTime.of(2026, 6, 30, 14, 0));
        overdue.setResolveDeadline(LocalDateTime.of(2026, 6, 30, 13, 0));

        ReportOverview overview = ReportCalculator.overview(List.of(normal, overdue));

        assertThat(overview.ticketCount()).isEqualTo(2);
        assertThat(overview.overdueRate()).isEqualTo(50.0);
        assertThat(overview.averageResolveHours()).isEqualTo(3.5);
    }
}
