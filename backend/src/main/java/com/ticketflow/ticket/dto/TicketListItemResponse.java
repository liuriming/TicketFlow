package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.enums.TicketPriority;
import com.ticketflow.ticket.enums.TicketStatus;
import com.ticketflow.ticket.enums.TicketAllowedAction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单列表项响应 DTO。
 *
 * @param id 工单 ID。
 * @param ticketNo 工单编号。
 * @param title 工单标题。
 * @param categoryId 工单分类 ID。
 * @param categoryName 工单分类名称。
 * @param priority 工单优先级。
 * @param status 当前工单状态。
 * @param creatorId 创建人用户 ID。
 * @param creatorName 创建人姓名。
 * @param creatorDeptName 创建人部门名称。
 * @param assigneeId 当前处理人用户 ID。
 * @param assigneeName 当前处理人姓名。
 * @param responseDeadline 响应截止时间。
 * @param resolveDeadline 处理截止时间。
 * @param createdAt 创建时间。
 * @param allowedActions 当前登录用户可执行的工单动作。
 */
public record TicketListItemResponse(
        Long id,
        String ticketNo,
        String title,
        Long categoryId,
        String categoryName,
        TicketPriority priority,
        TicketStatus status,
        Long creatorId,
        String creatorName,
        String creatorDeptName,
        Long assigneeId,
        String assigneeName,
        LocalDateTime responseDeadline,
        LocalDateTime resolveDeadline,
        LocalDateTime createdAt,
        List<TicketAllowedAction> allowedActions
) {
}
