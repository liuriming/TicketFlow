package com.ticketflow.audit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.audit.dto.OperationLogResponse;
import com.ticketflow.audit.entity.OperationLog;
import com.ticketflow.common.web.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 操作审计日志服务。
 *
 * <p>提供变更类请求日志落库和审计日志分页查询能力。</p>
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 记录一次 HTTP 请求操作。
     *
     * @param request HTTP 请求对象。
     * @param response HTTP 响应对象。
     * @param exception 请求处理异常，成功时为空。
     */
    void record(HttpServletRequest request, HttpServletResponse response, Exception exception);

    /**
     * 分页查询操作审计日志。
     *
     * @param pageNo 当前页码。
     * @param pageSize 每页数量。
     * @param keyword 请求路径或操作人关键字。
     * @return 审计日志分页结果。
     */
    PageResult<OperationLogResponse> pageLogs(long pageNo, long pageSize, String keyword);
}
