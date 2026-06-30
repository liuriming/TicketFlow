package com.ticketflow.report.dto;

/**
 * 工作台汇总指标响应 DTO。
 *
 * @param todayCreatedCount 今日新增工单数。
 * @param processingCount 当前处理中或待处理工单数。
 * @param closedCount 当前数据范围内已关闭工单数。
 * @param overdueCount 当前数据范围内已超时工单数。
 */
public record DashboardSummaryResponse(
        long todayCreatedCount,
        long processingCount,
        long closedCount,
        long overdueCount
) {
}
