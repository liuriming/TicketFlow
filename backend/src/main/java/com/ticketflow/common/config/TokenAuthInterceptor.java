package com.ticketflow.common.config;

import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录 token 拦截器。
 *
 * <p>第一版采用轻量级自定义 token，token 存放在 Redis 中。
 * 拦截器负责从请求头读取 token、解析当前登录用户，并将用户信息放入线程上下文，
 * 供后续数据权限和业务审计使用。</p>
 */
@Component
@RequiredArgsConstructor
public class TokenAuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        LoginUser loginUser = authService.parseToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录"));
        CurrentUserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserContext.clear();
    }
}
