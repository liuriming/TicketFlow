package com.ticketflow.sla.domain;

import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.ticket.entity.Ticket;

/**
 * SLA 通知事件工厂。
 *
 * <p>负责根据临期或超时工单生成站内信事件，保证定时任务和测试使用一致的消息文案。</p>
 */
public final class SlaNotificationFactory {

    private SlaNotificationFactory() {
    }

    /**
     * 创建处理超时通知事件。
     *
     * @param ticket 超时工单。
     * @return 通知事件。
     */
    public static NotificationEvent resolveOverdue(Ticket ticket) {
        Long receiverId = ticket.getAssigneeId() == null ? ticket.getCreatorId() : ticket.getAssigneeId();
        return new NotificationEvent(
                receiverId,
                "工单处理已超时",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已超过处理截止时间，请尽快跟进。",
                "SLA_RESOLVE_OVERDUE",
                ticket.getId(),
                "WARNING",
                "SLA_RESOLVE_OVERDUE:" + ticket.getId()
        );
    }

    /**
     * 创建响应超时通知事件。
     *
     * @param ticket 超时工单。
     * @return 通知事件。
     */
    public static NotificationEvent responseOverdue(Ticket ticket) {
        Long receiverId = ticket.getAssigneeId() == null ? ticket.getCreatorId() : ticket.getAssigneeId();
        return new NotificationEvent(
                receiverId,
                "工单响应已超时",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」尚未在响应时限内接单，请尽快处理。",
                "SLA_RESPONSE_OVERDUE",
                ticket.getId(),
                "WARNING",
                "SLA_RESPONSE_OVERDUE:" + ticket.getId()
        );
    }

    /**
     * 创建处理临期通知事件。
     *
     * @param ticket 临近处理截止时间的工单。
     * @return 通知事件。
     */
    public static NotificationEvent resolveWarning(Ticket ticket) {
        Long receiverId = ticket.getAssigneeId() == null ? ticket.getCreatorId() : ticket.getAssigneeId();
        return new NotificationEvent(
                receiverId,
                "工单处理即将超时",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」即将到达处理截止时间，请提前跟进。",
                "SLA_RESOLVE_WARNING",
                ticket.getId(),
                "WARNING",
                "SLA_RESOLVE_WARNING:" + ticket.getId()
        );
    }

    /**
     * 创建响应临期通知事件。
     *
     * @param ticket 临近响应截止时间的工单。
     * @return 通知事件。
     */
    public static NotificationEvent responseWarning(Ticket ticket) {
        Long receiverId = ticket.getAssigneeId() == null ? ticket.getCreatorId() : ticket.getAssigneeId();
        return new NotificationEvent(
                receiverId,
                "工单响应即将超时",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」即将到达响应截止时间，请及时接单。",
                "SLA_RESPONSE_WARNING",
                ticket.getId(),
                "WARNING",
                "SLA_RESPONSE_WARNING:" + ticket.getId()
        );
    }
}
