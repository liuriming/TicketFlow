package com.ticketflow.system.controller;

import com.ticketflow.common.web.ApiResult;
import com.ticketflow.system.dto.CurrentUserResponse;
import com.ticketflow.system.dto.LoginRequest;
import com.ticketflow.system.dto.LoginResponse;
import com.ticketflow.system.dto.MenuRouteResponse;
import com.ticketflow.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证接口。
 *
 * <p>提供登录、退出、当前用户信息和当前用户菜单路由能力。</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResult.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("Authorization"));
        return ApiResult.success();
    }

    @GetMapping("/me")
    public ApiResult<CurrentUserResponse> me() {
        return ApiResult.success(authService.currentUser());
    }

    @GetMapping("/routes")
    public ApiResult<List<MenuRouteResponse>> routes() {
        return ApiResult.success(authService.currentRoutes());
    }
}
