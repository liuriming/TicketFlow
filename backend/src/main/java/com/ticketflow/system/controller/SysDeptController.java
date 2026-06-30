package com.ticketflow.system.controller;

import com.ticketflow.common.web.ApiResult;
import com.ticketflow.system.annotation.RequirePermission;
import com.ticketflow.system.dto.SysDeptSaveRequest;
import com.ticketflow.system.entity.SysDept;
import com.ticketflow.system.service.SysDeptService;
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
 * 部门管理接口。
 *
 * <p>用于维护企业组织架构，并为数据权限提供部门路径基础数据。</p>
 */
@RestController
@RequestMapping("/api/system/depts")
@RequiredArgsConstructor
@RequirePermission("system:dept:list")
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping
    public ApiResult<List<SysDept>> list() {
        return ApiResult.success(deptService.list());
    }

    @GetMapping("/{id}")
    public ApiResult<SysDept> detail(@PathVariable Long id) {
        return ApiResult.success(deptService.getById(id));
    }

    @PostMapping
    public ApiResult<SysDept> create(@Valid @RequestBody SysDeptSaveRequest request) {
        return ApiResult.success(deptService.saveDept(null, request));
    }

    @PutMapping("/{id}")
    public ApiResult<SysDept> update(@PathVariable Long id, @Valid @RequestBody SysDeptSaveRequest request) {
        return ApiResult.success(deptService.saveDept(id, request));
    }
}
