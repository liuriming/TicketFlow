package com.ticketflow.system.dto;

import com.ticketflow.system.enums.DataScopeType;

import java.util.List;

/**
 * 角色详情响应 DTO。
 *
 * <p>用于系统管理页面回显角色基础资料、数据范围和菜单授权，避免前端编辑时丢失已绑定菜单。</p>
 *
 * @param id 角色 ID。
 * @param roleName 角色名称。
 * @param roleCode 角色编码。
 * @param dataScope 数据权限范围。
 * @param sortOrder 显示排序。
 * @param enabled 是否启用：1 启用，0 停用。
 * @param menuIds 已授权菜单 ID 集合。
 */
public record SysRoleDetailResponse(
        Long id,
        String roleName,
        String roleCode,
        DataScopeType dataScope,
        Integer sortOrder,
        Integer enabled,
        List<Long> menuIds
) {
}
