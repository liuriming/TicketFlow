package com.ticketflow.rule.dto;

import com.ticketflow.ticket.enums.TicketPriority;
import jakarta.validation.constraints.NotNull;

/**
 * SLA 规则保存请求 DTO。
 *
 * @param priority 工单优先级。
 * @param responseMinutes 响应时限，单位分钟。
 * @param resolveMinutes 处理时限，单位分钟。
 */
public record SlaRuleSaveRequest(
        @NotNull(message = "优先级不能为空")
        TicketPriority priority,
        @NotNull(message = "响应时限不能为空")
        Integer responseMinutes,
        @NotNull(message = "处理时限不能为空")
        Integer resolveMinutes
) {
}
