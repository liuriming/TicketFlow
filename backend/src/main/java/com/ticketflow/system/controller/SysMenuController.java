package com.ticketflow.system.controller;

import com.ticketflow.common.web.ApiResult;
import com.ticketflow.system.annotation.RequirePermission;
import com.ticketflow.system.dto.SysMenuSaveRequest;
import com.ticketflow.system.entity.SysMenu;
import com.ticketflow.system.service.SysMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单管理接口。
 *
 * <p>用于维护后台菜单路由和按钮权限标识。</p>
 */
@RestController
@RequestMapping("/api/system/menus")
@RequiredArgsConstructor
@RequirePermission("system:menu:list")
public class SysMenuController {

    private final SysMenuService menuService;

    @GetMapping
    public ApiResult<List<SysMenu>> list() {
        return ApiResult.success(menuService.list());
    }

    @GetMapping("/{id}")
    public ApiResult<SysMenu> detail(@PathVariable Long id) {
        return ApiResult.success(menuService.getById(id));
    }

    @PostMapping
    public ApiResult<SysMenu> create(@Valid @RequestBody SysMenuSaveRequest request) {
        return ApiResult.success(menuService.saveMenu(null, request));
    }

    @PutMapping("/{id}")
    public ApiResult<SysMenu> update(@PathVariable Long id, @Valid @RequestBody SysMenuSaveRequest request) {
        return ApiResult.success(menuService.saveMenu(id, request));
    }
}
