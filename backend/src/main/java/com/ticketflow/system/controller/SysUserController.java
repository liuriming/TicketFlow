package com.ticketflow.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ticketflow.common.web.ApiResult;
import com.ticketflow.common.web.PageResult;
import com.ticketflow.common.web.StatusUpdateRequest;
import com.ticketflow.system.annotation.RequirePermission;
import com.ticketflow.system.dto.SysUserDetailResponse;
import com.ticketflow.system.dto.SysUserSaveRequest;
import com.ticketflow.system.dto.UserPasswordResetRequest;
import com.ticketflow.system.dto.UserOptionResponse;
import com.ticketflow.system.entity.SysUser;
import com.ticketflow.system.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理接口。
 *
 * <p>用于系统管理员维护企业内部用户账号、所属部门和角色授权。</p>
 */
@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
@RequirePermission("system:user:list")
public class SysUserController {

    private final SysUserService userService;

    @GetMapping
    public ApiResult<PageResult<SysUserDetailResponse>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        IPage<SysUser> page = userService.page(Page.of(pageNo, pageSize));
        return ApiResult.success(new PageResult<>(
                page.getRecords().stream().map(this::toDetailResponse).toList(),
                page.getTotal(),
                pageNo,
                pageSize
        ));
    }

    @GetMapping("/options")
    @RequirePermission({"system:user:list", "ticket:transfer", "rule:dispatch:list"})
    public ApiResult<List<UserOptionResponse>> options() {
        return ApiResult.success(userService.listUserOptions());
    }

    @GetMapping("/{id}")
    public ApiResult<SysUserDetailResponse> detail(@PathVariable Long id) {
        return ApiResult.success(toDetailResponse(userService.getById(id)));
    }

    @PostMapping
    @RequirePermission("system:user:write")
    public ApiResult<SysUserDetailResponse> create(@Valid @RequestBody SysUserSaveRequest request) {
        return ApiResult.success(toDetailResponse(userService.saveUser(null, request)));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:user:write")
    public ApiResult<SysUserDetailResponse> update(@PathVariable Long id, @Valid @RequestBody SysUserSaveRequest request) {
        return ApiResult.success(toDetailResponse(userService.saveUser(id, request)));
    }

    @PutMapping("/{id}/status")
    @RequirePermission("system:user:write")
    public ApiResult<SysUserDetailResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ApiResult.success(toDetailResponse(userService.updateStatus(id, request.status())));
    }

    @PutMapping("/{id}/reset-password")
    @RequirePermission("system:user:write")
    public ApiResult<SysUserDetailResponse> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordResetRequest request
    ) {
        return ApiResult.success(toDetailResponse(userService.resetPassword(id, request.password())));
    }

    private SysUserDetailResponse toDetailResponse(SysUser user) {
        if (user == null) {
            return null;
        }
        return new SysUserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getPhone(),
                user.getEmail(),
                user.getDeptId(),
                user.getStatus(),
                userService.findUserRoleIds(user.getId())
        );
    }
}
