package com.ticketflow.audit.controller;

import com.ticketflow.audit.dto.OperationLogResponse;
import com.ticketflow.audit.service.OperationLogService;
import com.ticketflow.common.web.ApiResult;
import com.ticketflow.common.web.PageResult;
import com.ticketflow.system.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作审计日志接口。
 *
 * <p>提供管理员查询变更类接口操作记录的能力，不提供删除接口，避免审计日志被前端误删。</p>
 */
@RestController
@RequestMapping("/api/audit/logs")
@RequiredArgsConstructor
@RequirePermission("audit:log:list")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public ApiResult<PageResult<OperationLogResponse>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResult.success(operationLogService.pageLogs(pageNo, pageSize, keyword));
    }
}
