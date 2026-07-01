package com.ticketflow.common.config;

import com.ticketflow.audit.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 操作审计拦截器。
 *
 * <p>只记录 `/api/**` 下的 POST、PUT、DELETE 变更类请求，避免查询接口产生过多审计日志。
 * 登录和权限拦截器先完成用户解析后，本拦截器在请求结束时将操作信息交给审计服务落库。</p>
 */
@Component
@RequiredArgsConstructor
public class OperationLogInterceptor implements HandlerInterceptor {

    /**
     * 请求开始时间属性名，用于计算耗时。
     */
    public static final String START_TIME_ATTRIBUTE = "operationStartTime";

    private final OperationLogService operationLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        if (!shouldRecord(request)) {
            return;
        }
        operationLogService.record(request, response, exception);
    }

    private boolean shouldRecord(HttpServletRequest request) {
        String method = request.getMethod();
        return request.getRequestURI().startsWith("/api/")
                && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method));
    }
}
