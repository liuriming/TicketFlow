package com.ticketflow.sla.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.notification.service.NotificationPublisher;
import com.ticketflow.sla.domain.SlaNotificationFactory;
import com.ticketflow.sla.enums.SlaAlertType;
import com.ticketflow.sla.service.SlaAlertService;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.enums.TicketStatus;
import com.ticketflow.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SLA 超时扫描任务。
 *
 * <p>定时扫描未关闭、未取消的工单，对响应临期、响应超时、处理临期和处理超时分别发送
 * RabbitMQ 通知事件，并通过 SLA 告警记录避免重复推送。</p>
 */
@Component
@RequiredArgsConstructor
public class SlaMonitorJob {

    private static final long RESPONSE_WARNING_MINUTES = 30L;
    private static final long RESOLVE_WARNING_MINUTES = 120L;

    private final TicketMapper ticketMapper;
    private final NotificationPublisher notificationPublisher;
    private final SlaAlertService slaAlertService;

    @Scheduled(fixedDelay = 60_000)
    public void scanOverdueTickets() {
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> tickets = ticketMapper.selectList(Wrappers.<Ticket>lambdaQuery()
                .notIn(Ticket::getStatus, TicketStatus.CLOSED, TicketStatus.CANCELED));
        for (Ticket ticket : tickets) {
            scanResponseDeadline(ticket, now);
            scanResolveDeadline(ticket, now);
        }
    }

    private void scanResponseDeadline(Ticket ticket, LocalDateTime now) {
        if (ticket.getResponseDeadline() == null || ticket.getRespondedAt() != null) {
            return;
        }
        if (now.isAfter(ticket.getResponseDeadline())) {
            publishIfFirst(ticket.getId(), SlaAlertType.RESPONSE_OVERDUE, SlaNotificationFactory.responseOverdue(ticket));
            return;
        }
        if (Duration.between(now, ticket.getResponseDeadline()).toMinutes() <= RESPONSE_WARNING_MINUTES) {
            publishIfFirst(ticket.getId(), SlaAlertType.RESPONSE_WARNING, SlaNotificationFactory.responseWarning(ticket));
        }
    }

    private void scanResolveDeadline(Ticket ticket, LocalDateTime now) {
        if (ticket.getResolveDeadline() == null) {
            return;
        }
        if (now.isAfter(ticket.getResolveDeadline())) {
            publishIfFirst(ticket.getId(), SlaAlertType.RESOLVE_OVERDUE, SlaNotificationFactory.resolveOverdue(ticket));
            return;
        }
        if (Duration.between(now, ticket.getResolveDeadline()).toMinutes() <= RESOLVE_WARNING_MINUTES) {
            publishIfFirst(ticket.getId(), SlaAlertType.RESOLVE_WARNING, SlaNotificationFactory.resolveWarning(ticket));
        }
    }

    private void publishIfFirst(Long ticketId, SlaAlertType alertType, NotificationEvent event) {
        if (slaAlertService.markPublishedIfAbsent(ticketId, alertType)) {
            notificationPublisher.publish(event);
        }
    }
}
