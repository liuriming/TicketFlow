package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.enums.TicketStatus;

import java.time.LocalDateTime;

/**
 * 工单流转记录响应 DTO。
 *
 * @param id 记录 ID。
 * @param fromStatus 操作前状态。
 * @param toStatus 操作后状态。
 * @param operatorId 操作人用户 ID。
 * @param remark 操作说明。
 * @param createdAt 操作时间。
 */
public record TicketFlowRecordResponse(
        Long id,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        Long operatorId,
        String remark,
        LocalDateTime createdAt
) {
}
