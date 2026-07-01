package com.ticketflow.ticket.domain;

import com.ticketflow.notification.dto.NotificationEvent;
import com.ticketflow.ticket.entity.Ticket;

/**
 * 工单通知事件工厂。
 *
 * <p>集中维护工单协同通知的标题、内容、业务类型和幂等键，避免工单服务实现类中散落
 * 重复文案。工厂只负责根据工单快照生成通知事件，不负责发送和落库。</p>
 */
public final class TicketNotificationFactory {

    private TicketNotificationFactory() {
    }

    /**
     * 创建派单通知事件，发送给当前处理人。
     *
     * @param ticket 已分派处理人的工单。
     * @return 通知事件。
     */
    public static NotificationEvent assigned(Ticket ticket) {
        return new NotificationEvent(
                ticket.getAssigneeId(),
                "你有新的待接单工单",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已分派给你，请及时接单。",
                "TICKET_ASSIGNED",
                ticket.getId(),
                "INFO",
                "TICKET_ASSIGNED:" + ticket.getId() + ":" + ticket.getAssigneeId()
        );
    }

    /**
     * 创建转派通知事件，发送给新的处理人。
     *
     * @param ticket 已转派处理人的工单。
     * @return 通知事件。
     */
    public static NotificationEvent transferred(Ticket ticket) {
        return new NotificationEvent(
                ticket.getAssigneeId(),
                "工单已转派给你",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已转派给你，请及时跟进。",
                "TICKET_TRANSFERRED",
                ticket.getId(),
                "INFO",
                "TICKET_TRANSFERRED:" + ticket.getId() + ":" + ticket.getAssigneeId()
        );
    }

    /**
     * 创建接单通知事件，发送给工单创建人。
     *
     * @param ticket 已接单工单。
     * @return 通知事件。
     */
    public static NotificationEvent accepted(Ticket ticket) {
        return new NotificationEvent(
                ticket.getCreatorId(),
                "工单已接单",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已由运维人员接单。",
                "TICKET_ACCEPTED",
                ticket.getId(),
                "INFO",
                "TICKET_ACCEPTED:" + ticket.getId() + ":" + ticket.getCreatorId()
        );
    }

    /**
     * 创建待确认通知事件，发送给工单创建人。
     *
     * @param ticket 已提交处理结果的工单。
     * @return 通知事件。
     */
    public static NotificationEvent pendingConfirm(Ticket ticket) {
        return new NotificationEvent(
                ticket.getCreatorId(),
                "工单等待确认",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已提交处理结果，请确认是否关闭。",
                "TICKET_PENDING_CONFIRM",
                ticket.getId(),
                "INFO",
                "TICKET_PENDING_CONFIRM:" + ticket.getId() + ":" + ticket.getCreatorId()
        );
    }

    /**
     * 创建驳回通知事件，发送给当前处理人。
     *
     * @param ticket 被驳回的工单。
     * @return 通知事件。
     */
    public static NotificationEvent rejected(Ticket ticket) {
        return new NotificationEvent(
                ticket.getAssigneeId(),
                "处理结果被驳回",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」处理结果被驳回，请重新跟进。",
                "TICKET_REJECTED",
                ticket.getId(),
                "WARNING",
                "TICKET_REJECTED:" + ticket.getId() + ":" + ticket.getAssigneeId()
        );
    }

    /**
     * 创建关闭通知事件，发送给当前处理人。
     *
     * @param ticket 已关闭工单。
     * @return 通知事件。
     */
    public static NotificationEvent closed(Ticket ticket) {
        return new NotificationEvent(
                ticket.getAssigneeId(),
                "工单已确认关闭",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已由提交人确认关闭。",
                "TICKET_CLOSED",
                ticket.getId(),
                "INFO",
                "TICKET_CLOSED:" + ticket.getId() + ":" + ticket.getAssigneeId()
        );
    }

    /**
     * 创建取消通知事件，发送给指定参与人。
     *
     * @param ticket 已取消工单。
     * @param receiverId 接收人用户 ID。
     * @return 通知事件。
     */
    public static NotificationEvent canceled(Ticket ticket, Long receiverId) {
        return new NotificationEvent(
                receiverId,
                "工单已取消",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」已取消。",
                "TICKET_CANCELED",
                ticket.getId(),
                "INFO",
                "TICKET_CANCELED:" + ticket.getId() + ":" + receiverId
        );
    }

    /**
     * 创建评论通知事件，发送给除评论人以外的工单参与人。
     *
     * @param ticket 被评论的工单。
     * @param receiverId 接收人用户 ID。
     * @param commentId 评论记录 ID。
     * @return 通知事件。
     */
    public static NotificationEvent commented(Ticket ticket, Long receiverId, Long commentId) {
        return new NotificationEvent(
                receiverId,
                "工单有新评论",
                "工单 " + ticket.getTicketNo() + "「" + ticket.getTitle() + "」有新的沟通记录。",
                "TICKET_COMMENTED",
                ticket.getId(),
                "INFO",
                "TICKET_COMMENTED:" + ticket.getId() + ":" + commentId + ":" + receiverId
        );
    }
}
