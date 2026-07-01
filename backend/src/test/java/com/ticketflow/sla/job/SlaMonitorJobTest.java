package com.ticketflow.sla.job;

import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.notification.service.NotificationPublisher;
import com.ticketflow.sla.enums.SlaAlertType;
import com.ticketflow.sla.service.SlaAlertService;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.enums.TicketStatus;
import com.ticketflow.ticket.mapper.TicketMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SlaMonitorJobTest {

    private final TicketMapper ticketMapper = mock(TicketMapper.class);
    private final NotificationPublisher notificationPublisher = mock(NotificationPublisher.class);
    private final SlaAlertService slaAlertService = mock(SlaAlertService.class);

    @Test
    void 临期和超时告警只在首次命中时发布() {
        Ticket ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTicketNo("TF20260701001");
        ticket.setTitle("SLA 告警测试");
        ticket.setCreatorId(2L);
        ticket.setAssigneeId(3L);
        ticket.setStatus(TicketStatus.PROCESSING);
        ticket.setResponseDeadline(LocalDateTime.now().minusMinutes(1));
        ticket.setResolveDeadline(LocalDateTime.now().plusMinutes(30));

        when(ticketMapper.selectList(any())).thenReturn(List.of(ticket));
        when(slaAlertService.markPublishedIfAbsent(100L, SlaAlertType.RESPONSE_OVERDUE)).thenReturn(true);
        when(slaAlertService.markPublishedIfAbsent(100L, SlaAlertType.RESOLVE_WARNING)).thenReturn(false);

        SlaMonitorJob job = new SlaMonitorJob(ticketMapper, notificationPublisher, slaAlertService);
        job.scanOverdueTickets();

        verify(notificationPublisher, times(1)).publish(any(NotificationEvent.class));
    }
}
