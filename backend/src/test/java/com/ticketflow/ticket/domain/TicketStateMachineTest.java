package com.ticketflow.ticket.domain;

import com.ticketflow.ticket.enums.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStateMachineTest {

    @Test
    void 待接单工单可以流转为处理中() {
        assertThat(TicketStateMachine.canTransit(TicketStatus.PENDING_ACCEPT, TicketStatus.PROCESSING))
                .isTrue();
    }

    @Test
    void 已关闭工单不能重新流转为处理中() {
        assertThat(TicketStateMachine.canTransit(TicketStatus.CLOSED, TicketStatus.PROCESSING))
                .isFalse();
    }
}
