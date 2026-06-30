package com.ticketflow.ticket.dto;

import java.time.LocalDateTime;

/**
 * 工单评论响应 DTO。
 *
 * @param id 评论 ID。
 * @param userId 评论人用户 ID。
 * @param content 评论内容。
 * @param internalOnly 是否内部备注。
 * @param createdAt 评论时间。
 */
public record TicketCommentResponse(
        Long id,
        Long userId,
        String content,
        Integer internalOnly,
        LocalDateTime createdAt
) {
}
