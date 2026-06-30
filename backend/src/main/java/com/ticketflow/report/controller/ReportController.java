package com.ticketflow.report.controller;

import com.ticketflow.common.web.ApiResult;
import com.ticketflow.report.dto.ReportOverviewResponse;
import com.ticketflow.report.dto.WorkloadResponse;
import com.ticketflow.report.service.ReportService;
import com.ticketflow.system.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计报表接口。
 *
 * <p>用于工单数量、超时率、平均处理时长和人员工作量统计。</p>
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@RequirePermission("report:view")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    public ApiResult<ReportOverviewResponse> overview() {
        return ApiResult.success(reportService.overview());
    }

    @GetMapping("/workload")
    public ApiResult<List<WorkloadResponse>> workload() {
        return ApiResult.success(reportService.workload());
    }
}
