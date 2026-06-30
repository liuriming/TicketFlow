package com.ticketflow.sla.domain;

import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.ticket.entity.Ticket;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SlaNotificationFactoryTest {

    @Test
    void 处理超时时生成发给处理人的通知事件() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTicketNo("TF20260630001");
        ticket.setTitle("研发区网络间歇性中断");
        ticket.setAssigneeId(20L);
        ticket.setCreatorId(10L);
        ticket.setResolveDeadline(LocalDateTime.of(2026, 6, 30, 13, 0));

        NotificationEvent event = SlaNotificationFactory.resolveOverdue(ticket);

        assertThat(event.receiverId()).isEqualTo(20L);
        assertThat(event.businessType()).isEqualTo("SLA_RESOLVE_OVERDUE");
        assertThat(event.businessId()).isEqualTo(100L);
        assertThat(event.title()).isEqualTo("工单处理已超时");
        assertThat(event.content()).contains("TF20260630001").contains("研发区网络间歇性中断");
    }
}
