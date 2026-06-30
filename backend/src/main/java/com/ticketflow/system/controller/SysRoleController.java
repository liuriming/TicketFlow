package com.ticketflow.system.controller;

import com.ticketflow.common.web.ApiResult;
import com.ticketflow.common.web.StatusUpdateRequest;
import com.ticketflow.system.annotation.RequirePermission;
import com.ticketflow.system.dto.SysRoleDetailResponse;
import com.ticketflow.system.dto.SysRoleSaveRequest;
import com.ticketflow.system.entity.SysRole;
import com.ticketflow.system.service.SysRoleService;
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
 * 角色管理接口。
 *
 * <p>用于维护员工、运维工程师、运维主管、系统管理员等角色及其菜单权限。</p>
 */
@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
@RequirePermission("system:role:list")
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping
    public ApiResult<List<SysRoleDetailResponse>> list() {
        return ApiResult.success(roleService.list().stream().map(this::toDetailResponse).toList());
    }

    @GetMapping("/{id}")
    public ApiResult<SysRoleDetailResponse> detail(@PathVariable Long id) {
        return ApiResult.success(toDetailResponse(roleService.getById(id)));
    }

    @PostMapping
    @RequirePermission("system:role:write")
    public ApiResult<SysRoleDetailResponse> create(@Valid @RequestBody SysRoleSaveRequest request) {
        return ApiResult.success(toDetailResponse(roleService.saveRole(null, request)));
    }

    @PutMapping("/{id}")
    @RequirePermission("system:role:write")
    public ApiResult<SysRoleDetailResponse> update(@PathVariable Long id, @Valid @RequestBody SysRoleSaveRequest request) {
        return ApiResult.success(toDetailResponse(roleService.saveRole(id, request)));
    }

    @PutMapping("/{id}/enabled")
    @RequirePermission("system:role:write")
    public ApiResult<SysRoleDetailResponse> updateEnabled(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        return ApiResult.success(toDetailResponse(roleService.updateEnabled(id, request.enabled())));
    }

    private SysRoleDetailResponse toDetailResponse(SysRole role) {
        if (role == null) {
            return null;
        }
        return new SysRoleDetailResponse(
                role.getId(),
                role.getRoleName(),
                role.getRoleCode(),
                role.getDataScope(),
                role.getSortOrder(),
                role.getEnabled(),
                roleService.findRoleMenuIds(role.getId())
        );
    }
}
