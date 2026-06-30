package com.ticketflow.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联实体。
 *
 * <p>用于表达角色拥有的菜单和按钮权限，是菜单路由和接口权限判断的数据来源。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
public class SysRoleMenu extends BaseEntity {

    /**
     * 角色 ID。
     */
    private Long roleId;

    /**
     * 菜单 ID。
     */
    private Long menuId;
}
