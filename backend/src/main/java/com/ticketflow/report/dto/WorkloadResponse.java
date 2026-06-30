package com.ticketflow.report.dto;

/**
 * 人员工作量响应 DTO。
 *
 * @param assigneeId 处理人用户 ID。
 * @param processingCount 处理中或待确认工单数量。
 * @param closedCount 已关闭工单数量。
 * @param overdueCount 处理超时工单数量。
 */
public record WorkloadResponse(
        Long assigneeId,
        long processingCount,
        long closedCount,
        long overdueCount
) {
}
