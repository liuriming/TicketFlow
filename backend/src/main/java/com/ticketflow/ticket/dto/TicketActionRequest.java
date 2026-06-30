package com.ticketflow.ticket.dto;

/**
 * 工单操作请求 DTO。
 *
 * @param remark 操作说明，例如取消原因、驳回原因或确认备注。
 */
public record TicketActionRequest(String remark) {
}
