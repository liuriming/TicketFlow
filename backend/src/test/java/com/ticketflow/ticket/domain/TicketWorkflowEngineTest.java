package com.ticketflow.ticket.domain;

import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.entity.TicketFlowRecord;
import com.ticketflow.ticket.enums.TicketStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketWorkflowEngineTest {

    @Test
    void 指定处理人接单后工单进入处理中并记录首次响应时间() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setStatus(TicketStatus.PENDING_ACCEPT);
        ticket.setAssigneeId(20L);
        LocalDateTime acceptedAt = LocalDateTime.of(2026, 6, 30, 10, 0);

        TicketFlowRecord record = TicketWorkflowEngine.accept(ticket, 20L, acceptedAt);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PROCESSING);
        assertThat(ticket.getRespondedAt()).isEqualTo(acceptedAt);
        assertThat(record.getTicketId()).isEqualTo(100L);
        assertThat(record.getFromStatus()).isEqualTo(TicketStatus.PENDING_ACCEPT);
        assertThat(record.getToStatus()).isEqualTo(TicketStatus.PROCESSING);
        assertThat(record.getOperatorId()).isEqualTo(20L);
    }

    @Test
    void 非指定处理人不能接单() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setStatus(TicketStatus.PENDING_ACCEPT);
        ticket.setAssigneeId(20L);

        assertThatThrownBy(() -> TicketWorkflowEngine.accept(ticket, 21L, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("只有当前处理人可以接单");
    }

    @Test
    void 处理中提交处理结果后进入待确认() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setStatus(TicketStatus.PROCESSING);
        ticket.setAssigneeId(20L);

        TicketFlowRecord record = TicketWorkflowEngine.submitResult(ticket, 20L, "已重置账号权限");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PENDING_CONFIRM);
        assertThat(record.getRemark()).isEqualTo("已重置账号权限");
        assertThat(record.getToStatus()).isEqualTo(TicketStatus.PENDING_CONFIRM);
    }

    @Test
    void 创建人确认后工单关闭并记录关闭时间() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setStatus(TicketStatus.PENDING_CONFIRM);
        ticket.setCreatorId(10L);
        LocalDateTime closedAt = LocalDateTime.of(2026, 6, 30, 12, 0);

        TicketFlowRecord record = TicketWorkflowEngine.confirmClose(ticket, 10L, closedAt);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CLOSED);
        assertThat(ticket.getClosedAt()).isEqualTo(closedAt);
        assertThat(record.getToStatus()).isEqualTo(TicketStatus.CLOSED);
    }
}
