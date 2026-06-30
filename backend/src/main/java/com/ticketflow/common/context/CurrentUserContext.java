package com.ticketflow.common.context;

import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;

/**
 * 当前登录用户线程上下文。
 *
 * <p>用于在一次 HTTP 请求生命周期内传递登录用户信息，避免控制器和服务层重复解析 token。
 * 请求结束时必须清理，防止线程复用导致用户信息串号。</p>
 */
public final class CurrentUserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser getRequired() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        return loginUser;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
