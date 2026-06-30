package com.ticketflow.system.service;

import com.ticketflow.common.context.LoginUser;
import com.ticketflow.system.dto.CurrentUserResponse;
import com.ticketflow.system.dto.LoginRequest;
import com.ticketflow.system.dto.LoginResponse;
import com.ticketflow.system.dto.MenuRouteResponse;

import java.util.List;
import java.util.Optional;

/**
 * 认证服务接口。
 *
 * <p>负责登录、退出、token 解析、当前用户信息和当前用户菜单路由查询。</p>
 */
public interface AuthService {

    /**
     * 用户登录。
     *
     * @param request 登录请求。
     * @return 登录响应，包含 token 和有效期。
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户退出登录。
     *
     * @param token 当前请求 token。
     */
    void logout(String token);

    /**
     * 解析 token 并返回登录用户快照。
     *
     * @param token 访问令牌。
     * @return 登录用户快照。
     */
    Optional<LoginUser> parseToken(String token);

    /**
     * 查询当前登录用户信息。
     *
     * @return 当前用户响应。
     */
    CurrentUserResponse currentUser();

    /**
     * 查询当前登录用户可访问的菜单路由。
     *
     * @return 菜单路由树。
     */
    List<MenuRouteResponse> currentRoutes();
}
