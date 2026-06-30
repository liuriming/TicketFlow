package com.ticketflow.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色关联实体。
 *
 * <p>用于表达一个用户拥有的多个角色，是 RBAC 授权模型的基础关联表。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 角色 ID。
     */
    private Long roleId;
}
