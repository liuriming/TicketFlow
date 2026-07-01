package com.ticketflow.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.audit.dto.OperationLogResponse;
import com.ticketflow.audit.entity.OperationLog;
import com.ticketflow.audit.mapper.OperationLogMapper;
import com.ticketflow.audit.service.OperationLogService;
import com.ticketflow.common.config.OperationLogInterceptor;
import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.web.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 操作审计日志服务实现类。
 *
 * <p>从请求上下文中提取当前用户、请求路径、客户端 IP、响应状态和耗时并写入数据库。
 * 查询接口仅提供只读分页能力，审计日志不提供前端删除入口。</p>
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        OperationLog log = new OperationLog();
        LoginUser loginUser = currentUserOrNull();
        if (loginUser != null) {
            log.setOperatorId(loginUser.userId());
            log.setOperatorName(loginUser.realName());
        }
        log.setRequestMethod(request.getMethod());
        log.setRequestUri(request.getRequestURI());
        log.setQueryString(request.getQueryString());
        log.setClientIp(resolveClientIp(request));
        log.setSuccess(exception == null && response.getStatus() < 400 ? 1 : 0);
        log.setErrorMessage(exception == null ? null : exception.getMessage());
        log.setDurationMs(resolveDurationMs(request));
        save(log);
    }

    @Override
    public PageResult<OperationLogResponse> pageLogs(long pageNo, long pageSize, String keyword) {
        LambdaQueryWrapper<OperationLog> query = Wrappers.lambdaQuery(OperationLog.class);
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(OperationLog::getRequestUri, keyword)
                    .or()
                    .like(OperationLog::getOperatorName, keyword));
        }
        query.orderByDesc(OperationLog::getCreatedAt).orderByDesc(OperationLog::getId);
        IPage<OperationLog> page = page(Page.of(pageNo, pageSize), query);
        List<OperationLogResponse> records = page.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return new PageResult<>(records, page.getTotal(), pageNo, pageSize);
    }

    private LoginUser currentUserOrNull() {
        try {
            return CurrentUserContext.getRequired();
        } catch (BusinessException exception) {
            return null;
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    private Long resolveDurationMs(HttpServletRequest request) {
        Object startTime = request.getAttribute(OperationLogInterceptor.START_TIME_ATTRIBUTE);
        if (startTime instanceof Long start) {
            return Math.max(0L, System.currentTimeMillis() - start);
        }
        return 0L;
    }

    private OperationLogResponse toResponse(OperationLog log) {
        return new OperationLogResponse(
                log.getId(),
                log.getOperatorId(),
                log.getOperatorName(),
                log.getRequestMethod(),
                log.getRequestUri(),
                log.getQueryString(),
                log.getClientIp(),
                log.getSuccess(),
                log.getErrorMessage(),
                log.getDurationMs(),
                log.getCreatedAt()
        );
    }
}
