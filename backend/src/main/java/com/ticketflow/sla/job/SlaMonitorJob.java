package com.ticketflow.sla.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ticketflow.notification.service.NotificationPublisher;
import com.ticketflow.sla.domain.SlaNotificationFactory;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.enums.TicketStatus;
import com.ticketflow.ticket.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SLA 超时扫描任务。
 *
 * <p>定时扫描未关闭、未取消的工单，对响应超时和处理超时分别发送 RabbitMQ 通知事件。</p>
 */
@Component
@RequiredArgsConstructor
public class SlaMonitorJob {

    private final TicketMapper ticketMapper;
    private final NotificationPublisher notificationPublisher;

    @Scheduled(fixedDelay = 60_000)
    public void scanOverdueTickets() {
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> tickets = ticketMapper.selectList(Wrappers.<Ticket>lambdaQuery()
                .notIn(Ticket::getStatus, TicketStatus.CLOSED, TicketStatus.CANCELED));
        for (Ticket ticket : tickets) {
            if (ticket.getResponseDeadline() != null
                    && ticket.getRespondedAt() == null
                    && now.isAfter(ticket.getResponseDeadline())) {
                notificationPublisher.publish(SlaNotificationFactory.responseOverdue(ticket));
            }
            if (ticket.getResolveDeadline() != null
                    && now.isAfter(ticket.getResolveDeadline())) {
                notificationPublisher.publish(SlaNotificationFactory.resolveOverdue(ticket));
            }
        }
    }
}
