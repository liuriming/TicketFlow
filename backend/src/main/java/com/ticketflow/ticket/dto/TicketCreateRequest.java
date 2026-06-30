package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建工单请求 DTO。
 *
 * @param title 工单标题，用于列表快速识别问题。
 * @param description 工单详细描述，记录报修现象、影响范围和期望结果。
 * @param categoryId 工单分类 ID，用于派单规则和 SLA 规则匹配。
 * @param priority 工单优先级，用于计算响应和处理时限。
 */
public record TicketCreateRequest(
        @NotBlank(message = "工单标题不能为空")
        String title,
        @NotBlank(message = "工单描述不能为空")
        String description,
        @NotNull(message = "工单分类不能为空")
        Long categoryId,
        @NotNull(message = "工单优先级不能为空")
        TicketPriority priority
) {
}
