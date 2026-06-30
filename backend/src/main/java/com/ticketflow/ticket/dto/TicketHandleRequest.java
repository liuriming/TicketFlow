package com.ticketflow.ticket.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 工单处理请求 DTO。
 *
 * @param result 处理结果说明，提交后工单进入待确认状态。
 */
public record TicketHandleRequest(
        @NotBlank(message = "处理结果不能为空")
        String result
) {
}
