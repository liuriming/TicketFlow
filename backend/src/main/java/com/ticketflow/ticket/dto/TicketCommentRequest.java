package com.ticketflow.ticket.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 工单评论请求 DTO。
 *
 * @param content 评论内容。
 * @param internalOnly 是否内部备注，true 表示仅运维内部可见。
 */
public record TicketCommentRequest(
        @NotBlank(message = "评论内容不能为空")
        String content,
        Boolean internalOnly
) {
}
