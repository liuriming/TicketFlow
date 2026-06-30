package com.ticketflow.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import com.ticketflow.system.enums.MenuType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体。
 *
 * <p>菜单同时支撑前端路由和按钮权限。目录、菜单、按钮通过 type 字段区分，
 * 权限标识用于后端接口或前端按钮级权限判断。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /**
     * 父菜单 ID，根菜单为 0。
     */
    private Long parentId;

    /**
     * 菜单名称，前端侧边栏展示使用。
     */
    private String menuName;

    /**
     * 菜单类型：目录、菜单、按钮。
     */
    private MenuType type;

    /**
     * 前端路由路径。
     */
    private String path;

    /**
     * 前端组件路径。
     */
    private String component;

    /**
     * 菜单图标名称。
     */
    private String icon;

    /**
     * 权限标识，例如 system:user:list。
     */
    private String permission;

    /**
     * 显示排序，数值越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 是否显示在菜单中：1 显示，0 隐藏。
     */
    private Integer visible;
}
