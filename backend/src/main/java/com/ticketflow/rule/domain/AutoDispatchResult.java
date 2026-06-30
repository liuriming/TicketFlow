package com.ticketflow.rule.domain;

import com.ticketflow.ticket.enums.TicketStatus;

/**
 * 自动派单结果。
 *
 * @param assigned 是否成功派单。
 * @param assigneeId 选中的处理人用户 ID。
 * @param targetStatus 派单后的目标状态。
 * @param reason 派单结果说明。
 */
public record AutoDispatchResult(
        boolean assigned,
        Long assigneeId,
        TicketStatus targetStatus,
        String reason
) {
}
