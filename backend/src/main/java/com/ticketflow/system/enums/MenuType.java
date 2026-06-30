package com.ticketflow.system.enums;

/**
 * 菜单类型。
 *
 * <p>目录用于承载子菜单，菜单用于前端路由，按钮用于接口级或页面操作权限。</p>
 */
public enum MenuType {

    /**
     * 菜单目录。
     */
    CATALOG,

    /**
     * 路由菜单。
     */
    MENU,

    /**
     * 操作按钮。
     */
    BUTTON
}
