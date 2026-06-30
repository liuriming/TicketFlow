package com.ticketflow.common.config;

import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.context.LoginUser;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.system.annotation.RequirePermission;
import com.ticketflow.system.domain.PermissionMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 接口权限拦截器。
 *
 * <p>登录拦截器完成 token 解析后，该拦截器读取控制器上的 {@link RequirePermission} 注解，
 * 并根据当前用户权限集合判断是否允许访问。未标注权限的接口只要求登录。</p>
 */
@Component
public class PermissionAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequirePermission requirePermission = resolvePermission(handlerMethod);
        if (requirePermission == null) {
            return true;
        }
        LoginUser loginUser = CurrentUserContext.getRequired();
        if (!PermissionMatcher.hasAnyPermission(loginUser.permissions(), requirePermission.value())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有访问该接口的权限");
        }
        return true;
    }

    private RequirePermission resolvePermission(HandlerMethod handlerMethod) {
        RequirePermission methodPermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (methodPermission != null) {
            return methodPermission;
        }
        return handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
    }
}
