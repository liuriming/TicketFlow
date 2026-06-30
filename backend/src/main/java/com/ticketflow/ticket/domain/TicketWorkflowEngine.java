package com.ticketflow.ticket.domain;

import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.ticket.entity.Ticket;
import com.ticketflow.ticket.entity.TicketFlowRecord;
import com.ticketflow.ticket.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 工单流程领域引擎。
 *
 * <p>集中处理接单、提交处理结果、确认关闭、驳回、取消和转派等状态变更规则。
 * 该类只修改传入的工单对象并返回流转记录，不依赖数据库，便于单元测试和服务层复用。</p>
 */
public final class TicketWorkflowEngine {

    private TicketWorkflowEngine() {
    }

    /**
     * 工单接单。
     *
     * @param ticket 待接单工单。
     * @param operatorId 操作人用户 ID。
     * @param acceptedAt 接单时间。
     * @return 工单流转记录。
     */
    public static TicketFlowRecord accept(Ticket ticket, Long operatorId, LocalDateTime acceptedAt) {
        requireAssignee(ticket, operatorId, "只有当前处理人可以接单");
        return transit(ticket, TicketStatus.PROCESSING, operatorId, "运维人员接单", acceptedAt, true, false);
    }

    /**
     * 提交处理结果。
     *
     * @param ticket 处理中工单。
     * @param operatorId 操作人用户 ID。
     * @param result 处理结果说明。
     * @return 工单流转记录。
     */
    public static TicketFlowRecord submitResult(Ticket ticket, Long operatorId, String result) {
        requireAssignee(ticket, operatorId, "只有当前处理人可以提交处理结果");
        return transit(ticket, TicketStatus.PENDING_CONFIRM, operatorId, result, LocalDateTime.now(), false, false);
    }

    /**
     * 确认关闭工单。
     *
     * @param ticket 待确认工单。
     * @param operatorId 操作人用户 ID。
     * @param closedAt 关闭时间。
     * @return 工单流转记录。
     */
    public static TicketFlowRecord confirmClose(Ticket ticket, Long operatorId, LocalDateTime closedAt) {
        requireCreator(ticket, operatorId, "只有工单创建人可以确认关闭");
        return transit(ticket, TicketStatus.CLOSED, operatorId, "确认关闭工单", closedAt, false, true);
    }

    /**
     * 驳回处理结果。
     *
     * @param ticket 待确认工单。
     * @param operatorId 操作人用户 ID。
     * @param reason 驳回原因。
     * @return 工单流转记录。
     */
    public static TicketFlowRecord reject(Ticket ticket, Long operatorId, String reason) {
        requireCreator(ticket, operatorId, "只有工单创建人可以驳回处理结果");
        return transit(ticket, TicketStatus.REJECTED, operatorId, reason, LocalDateTime.now(), false, false);
    }

    /**
     * 取消工单。
     *
     * @param ticket 待取消工单。
     * @param operatorId 操作人用户 ID。
     * @param reason 取消原因。
     * @return 工单流转记录。
     */
    public static TicketFlowRecord cancel(Ticket ticket, Long operatorId, String reason) {
        requireCreator(ticket, operatorId, "只有工单创建人可以取消工单");
        return transit(ticket, TicketStatus.CANCELED, operatorId, reason, LocalDateTime.now(), false, true);
    }

    /**
     * 转派工单。
     *
     * @param ticket 待转派工单。
     * @param newAssigneeId 新处理人用户 ID。
     * @param operatorId 操作人用户 ID。
     * @param reason 转派原因。
     * @return 工单流转记录。
     */
    public static TicketFlowRecord transfer(Ticket ticket, Long newAssigneeId, Long operatorId, String reason) {
        TicketStatus fromStatus = ticket.getStatus();
        if (fromStatus == TicketStatus.CLOSED || fromStatus == TicketStatus.CANCELED) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "终态工单不能转派");
        }
        ticket.setAssigneeId(newAssigneeId);
        ticket.setStatus(TicketStatus.PENDING_ACCEPT);
        return buildRecord(ticket.getId(), fromStatus, TicketStatus.PENDING_ACCEPT, operatorId, reason);
    }

    private static TicketFlowRecord transit(
            Ticket ticket,
            TicketStatus targetStatus,
            Long operatorId,
            String remark,
            LocalDateTime operatedAt,
            boolean markResponded,
            boolean markClosed
    ) {
        TicketStatus fromStatus = ticket.getStatus();
        if (!TicketStateMachine.canTransit(fromStatus, targetStatus)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "当前工单状态不允许执行该操作");
        }
        ticket.setStatus(targetStatus);
        if (markResponded && ticket.getRespondedAt() == null) {
            ticket.setRespondedAt(operatedAt);
        }
        if (markClosed) {
            ticket.setClosedAt(operatedAt);
        }
        return buildRecord(ticket.getId(), fromStatus, targetStatus, operatorId, remark);
    }

    private static TicketFlowRecord buildRecord(
            Long ticketId,
            TicketStatus fromStatus,
            TicketStatus toStatus,
            Long operatorId,
            String remark
    ) {
        TicketFlowRecord record = new TicketFlowRecord();
        record.setTicketId(ticketId);
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setOperatorId(operatorId);
        record.setRemark(remark);
        return record;
    }

    private static void requireAssignee(Ticket ticket, Long operatorId, String message) {
        if (!Objects.equals(ticket.getAssigneeId(), operatorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, message);
        }
    }

    private static void requireCreator(Ticket ticket, Long operatorId, String message) {
        if (!Objects.equals(ticket.getCreatorId(), operatorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, message);
        }
    }
}
