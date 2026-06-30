package com.ticketflow.system.dto;

import java.util.List;

/**
 * 菜单路由响应 DTO。
 *
 * @param id 菜单 ID。
 * @param parentId 父菜单 ID。
 * @param name 菜单名称。
 * @param path 前端路由路径。
 * @param component 前端组件路径。
 * @param icon 菜单图标。
 * @param permission 权限标识。
 * @param children 子菜单集合。
 */
public record MenuRouteResponse(
        Long id,
        Long parentId,
        String name,
        String path,
        String component,
        String icon,
        String permission,
        List<MenuRouteResponse> children
) {
}
