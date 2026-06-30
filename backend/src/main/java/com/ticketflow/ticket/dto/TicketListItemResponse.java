package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.enums.TicketPriority;
import com.ticketflow.ticket.enums.TicketStatus;

import java.time.LocalDateTime;

/**
 * 工单列表项响应 DTO。
 *
 * @param id 工单 ID。
 * @param ticketNo 工单编号。
 * @param title 工单标题。
 * @param categoryId 工单分类 ID。
 * @param priority 工单优先级。
 * @param status 当前工单状态。
 * @param creatorId 创建人用户 ID。
 * @param assigneeId 当前处理人用户 ID。
 * @param responseDeadline 响应截止时间。
 * @param resolveDeadline 处理截止时间。
 * @param createdAt 创建时间。
 */
public record TicketListItemResponse(
        Long id,
        String ticketNo,
        String title,
        Long categoryId,
        TicketPriority priority,
        TicketStatus status,
        Long creatorId,
        Long assigneeId,
        LocalDateTime responseDeadline,
        LocalDateTime resolveDeadline,
        LocalDateTime createdAt
) {
}
