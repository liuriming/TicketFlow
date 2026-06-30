package com.ticketflow.report.domain;

/**
 * 报表概览领域结果。
 *
 * @param ticketCount 工单总数。
 * @param overdueRate 超时率，百分比数值。
 * @param averageResolveHours 平均处理时长，单位小时。
 */
public record ReportOverview(long ticketCount, double overdueRate, double averageResolveHours) {
}
