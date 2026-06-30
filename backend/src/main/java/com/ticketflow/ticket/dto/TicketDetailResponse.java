package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.enums.TicketPriority;
import com.ticketflow.ticket.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单详情响应 DTO。
 *
 * @param id 工单 ID。
 * @param ticketNo 工单编号。
 * @param title 工单标题。
 * @param description 工单描述。
 * @param categoryId 分类 ID。
 * @param priority 优先级。
 * @param status 当前状态。
 * @param creatorId 创建人用户 ID。
 * @param creatorDeptId 创建人部门 ID。
 * @param assigneeId 当前处理人用户 ID。
 * @param responseDeadline 响应截止时间。
 * @param resolveDeadline 处理截止时间。
 * @param respondedAt 实际响应时间。
 * @param closedAt 实际关闭时间。
 * @param flowRecords 流转记录列表。
 * @param comments 评论列表。
 */
public record TicketDetailResponse(
        Long id,
        String ticketNo,
        String title,
        String description,
        Long categoryId,
        TicketPriority priority,
        TicketStatus status,
        Long creatorId,
        Long creatorDeptId,
        Long assigneeId,
        LocalDateTime responseDeadline,
        LocalDateTime resolveDeadline,
        LocalDateTime respondedAt,
        LocalDateTime closedAt,
        List<TicketFlowRecordResponse> flowRecords,
        List<TicketCommentResponse> comments
) {
}
