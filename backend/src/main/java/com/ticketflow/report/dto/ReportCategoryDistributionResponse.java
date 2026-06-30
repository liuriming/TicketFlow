package com.ticketflow.report.dto;

/**
 * 工单分类分布响应 DTO。
 *
 * @param categoryId 分类 ID。
 * @param categoryName 分类名称。
 * @param ticketCount 工单数量。
 * @param percentage 当前分类在全部工单中的占比。
 */
public record ReportCategoryDistributionResponse(
        Long categoryId,
        String categoryName,
        long ticketCount,
        double percentage
) {
}
