package com.ticketflow.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import com.ticketflow.system.enums.DataScopeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体。
 *
 * <p>角色用于聚合菜单权限和数据权限。一个用户可以拥有多个角色，
 * 登录时会合并角色权限并取最大数据范围。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色名称，例如“运维工程师”。
     */
    private String roleName;

    /**
     * 角色编码，例如 OPS_ENGINEER。
     */
    private String roleCode;

    /**
     * 数据权限范围。
     */
    private DataScopeType dataScope;

    /**
     * 显示排序，数值越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 角色状态：1 启用，0 停用。
     */
    private Integer enabled;
}
