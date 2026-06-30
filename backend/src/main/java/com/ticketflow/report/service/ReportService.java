package com.ticketflow.report.service;

import com.ticketflow.report.dto.ReportOverviewResponse;
import com.ticketflow.report.dto.DashboardSummaryResponse;
import com.ticketflow.report.dto.ReportCategoryDistributionResponse;
import com.ticketflow.report.dto.WorkloadResponse;

import java.util.List;

/**
 * 报表服务接口。
 *
 * <p>提供工单数量、超时率、平均处理时长和人员工作量统计能力。</p>
 */
public interface ReportService {

    /**
     * 查询工单统计概览。
     *
     * @return 概览指标。
     */
    ReportOverviewResponse overview();

    /**
     * 查询工作台汇总指标。
     *
     * @return 工作台指标。
     */
    DashboardSummaryResponse dashboard();

    /**
     * 查询工单分类分布。
     *
     * @return 分类分布列表。
     */
    List<ReportCategoryDistributionResponse> categoryDistribution();

    /**
     * 查询人员工作量。
     *
     * @return 人员工作量列表。
     */
    List<WorkloadResponse> workload();
}
