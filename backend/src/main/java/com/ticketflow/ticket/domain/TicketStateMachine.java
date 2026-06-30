package com.ticketflow.ticket.domain;

import com.ticketflow.ticket.enums.TicketStatus;

import java.util.Map;
import java.util.Set;

/**
 * 工单状态机。
 *
 * <p>集中维护工单状态允许流转关系，避免不同接口各自判断导致流程不一致。</p>
 */
public final class TicketStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> TRANSITIONS = Map.of(
            TicketStatus.CREATED, Set.of(TicketStatus.PENDING_ASSIGN, TicketStatus.CANCELED),
            TicketStatus.PENDING_ASSIGN, Set.of(TicketStatus.PENDING_ACCEPT, TicketStatus.CANCELED),
            TicketStatus.PENDING_ACCEPT, Set.of(TicketStatus.PROCESSING, TicketStatus.PENDING_ASSIGN, TicketStatus.CANCELED),
            TicketStatus.PROCESSING, Set.of(TicketStatus.PENDING_CONFIRM, TicketStatus.PENDING_ASSIGN),
            TicketStatus.PENDING_CONFIRM, Set.of(TicketStatus.CLOSED, TicketStatus.REJECTED),
            TicketStatus.REJECTED, Set.of(TicketStatus.PROCESSING, TicketStatus.PENDING_ASSIGN),
            TicketStatus.CLOSED, Set.of(),
            TicketStatus.CANCELED, Set.of()
    );

    private TicketStateMachine() {
    }

    /**
     * 判断工单能否从当前状态流转到目标状态。
     *
     * @param current 当前状态。
     * @param target 目标状态。
     * @return true 表示允许流转。
     */
    public static boolean canTransit(TicketStatus current, TicketStatus target) {
        if (current == null || target == null) {
            return false;
        }
        return TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
    }
}
