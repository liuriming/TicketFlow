package com.ticketflow.ticket.domain;

import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.ticket.entity.Ticket;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketNotificationFactoryTest {

    @Test
    void 派单通知发送给处理人并使用稳定幂等键() {
        Ticket ticket = ticket();

        NotificationEvent event = TicketNotificationFactory.assigned(ticket);

        assertThat(event.receiverId()).isEqualTo(3L);
        assertThat(event.title()).isEqualTo("你有新的待接单工单");
        assertThat(event.businessType()).isEqualTo("TICKET_ASSIGNED");
        assertThat(event.businessId()).isEqualTo(100L);
        assertThat(event.level()).isEqualTo("INFO");
        assertThat(event.dedupeKey()).isEqualTo("TICKET_ASSIGNED:100:3");
    }

    @Test
    void 处理完成通知发送给创建人() {
        Ticket ticket = ticket();

        NotificationEvent event = TicketNotificationFactory.pendingConfirm(ticket);

        assertThat(event.receiverId()).isEqualTo(2L);
        assertThat(event.title()).isEqualTo("工单等待确认");
        assertThat(event.businessType()).isEqualTo("TICKET_PENDING_CONFIRM");
        assertThat(event.dedupeKey()).isEqualTo("TICKET_PENDING_CONFIRM:100:2");
    }

    @Test
    void 评论通知按评论记录生成独立幂等键() {
        Ticket ticket = ticket();

        NotificationEvent event = TicketNotificationFactory.commented(ticket, 3L, 900L);

        assertThat(event.receiverId()).isEqualTo(3L);
        assertThat(event.title()).isEqualTo("工单有新评论");
        assertThat(event.businessType()).isEqualTo("TICKET_COMMENTED");
        assertThat(event.dedupeKey()).isEqualTo("TICKET_COMMENTED:100:900:3");
    }

    private Ticket ticket() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTicketNo("TF20260701001");
        ticket.setTitle("第三阶段通知工单");
        ticket.setCreatorId(2L);
        ticket.setAssigneeId(3L);
        return ticket;
    }
}
