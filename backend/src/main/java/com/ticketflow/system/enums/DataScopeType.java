package com.ticketflow.system.enums;

/**
 * 数据权限范围。
 *
 * <p>用于控制用户能看到哪些业务数据，默认从角色中取权限范围最大的配置。</p>
 */
public enum DataScopeType {

    /**
     * 仅本人数据，例如员工只能查看自己提交的工单。
     */
    SELF,

    /**
     * 本部门数据，例如主管查看所在部门工单。
     */
    DEPT,

    /**
     * 本部门及下级部门数据，例如运维主管查看组织树下所有运维数据。
     */
    DEPT_AND_CHILD,

    /**
     * 全部数据，一般仅系统管理员使用。
     */
    ALL
}
