package com.ticketflow.system.dto;

import com.ticketflow.system.enums.DataScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 角色保存请求 DTO。
 *
 * @param roleName 角色名称。
 * @param roleCode 角色编码。
 * @param dataScope 数据权限范围。
 * @param menuIds 授权菜单 ID 集合。
 */
public record SysRoleSaveRequest(
        @NotBlank(message = "角色名称不能为空")
        String roleName,
        @NotBlank(message = "角色编码不能为空")
        String roleCode,
        @NotNull(message = "数据权限不能为空")
        DataScopeType dataScope,
        List<Long> menuIds
) {
}
