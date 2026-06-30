package com.ticketflow.rule.domain;

import com.ticketflow.ticket.enums.TicketStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AutoDispatchPlannerTest {

    @Test
    void 按分类部门和当前负载选择处理人并进入待接单() {
        AutoDispatchTicket ticket = new AutoDispatchTicket(100L, 1L, 2L, "NETWORK");
        AutoDispatchRule busyRule = new AutoDispatchRule(1L, 1L, 2L, "NETWORK", 20L, 10);
        AutoDispatchRule idleRule = new AutoDispatchRule(2L, 1L, 2L, "NETWORK", 21L, 10);

        AutoDispatchResult result = AutoDispatchPlanner.plan(
                ticket,
                List.of(busyRule, idleRule),
                Map.of(20L, 5, 21L, 1)
        );

        assertThat(result.assigneeId()).isEqualTo(21L);
        assertThat(result.targetStatus()).isEqualTo(TicketStatus.PENDING_ACCEPT);
        assertThat(result.assigned()).isTrue();
    }

    @Test
    void 没有匹配规则时保持待分派() {
        AutoDispatchTicket ticket = new AutoDispatchTicket(100L, 1L, 2L, "NETWORK");

        AutoDispatchResult result = AutoDispatchPlanner.plan(
                ticket,
                List.of(new AutoDispatchRule(1L, 2L, 2L, "SERVER", 20L, 10)),
                Map.of(20L, 0)
        );

        assertThat(result.assigneeId()).isNull();
        assertThat(result.targetStatus()).isEqualTo(TicketStatus.PENDING_ASSIGN);
        assertThat(result.assigned()).isFalse();
    }
}
