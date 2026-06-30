package com.ticketflow.ticket.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 工单转派请求 DTO。
 *
 * @param assigneeId 新处理人用户 ID。
 * @param reason 转派原因，用于工单流转记录。
 */
public record TicketTransferRequest(
        @NotNull(message = "新处理人不能为空")
        Long assigneeId,
        String reason
) {
}
