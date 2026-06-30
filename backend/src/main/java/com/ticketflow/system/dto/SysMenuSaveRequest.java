package com.ticketflow.system.dto;

import com.ticketflow.system.enums.MenuType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 菜单保存请求 DTO。
 *
 * @param parentId 父菜单 ID，根菜单为 0。
 * @param menuName 菜单名称。
 * @param type 菜单类型。
 * @param path 路由路径。
 * @param component 前端组件路径。
 * @param icon 图标名称。
 * @param permission 权限标识。
 * @param sortOrder 显示排序。
 * @param visible 是否在菜单中显示。
 */
public record SysMenuSaveRequest(
        Long parentId,
        @NotBlank(message = "菜单名称不能为空")
        String menuName,
        @NotNull(message = "菜单类型不能为空")
        MenuType type,
        String path,
        String component,
        String icon,
        String permission,
        Integer sortOrder,
        Integer visible
) {
}
