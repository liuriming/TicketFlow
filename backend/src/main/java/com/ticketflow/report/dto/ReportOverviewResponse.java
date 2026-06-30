package com.ticketflow.report.dto;

/**
 * 报表概览响应 DTO。
 *
 * @param ticketCount 工单总数。
 * @param overdueRate 超时率，百分比数值。
 * @param averageResolveHours 平均处理时长，单位小时。
 * @param activeAssigneeCount 当前有工单处理人的数量。
 */
public record ReportOverviewResponse(
        long ticketCount,
        double overdueRate,
        double averageResolveHours,
        long activeAssigneeCount
) {
}
