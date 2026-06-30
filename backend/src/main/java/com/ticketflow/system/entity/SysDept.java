package com.ticketflow.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体。
 *
 * <p>部门用于组织用户、控制数据权限范围，并通过 path 字段表达上下级关系。
 * path 示例为 1/2/5，便于判断“本部门及下级”的数据范围。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    /**
     * 父部门 ID，根部门为 0。
     */
    private Long parentId;

    /**
     * 部门名称。
     */
    private String deptName;

    /**
     * 部门层级路径，用斜杠连接祖先部门 ID。
     */
    private String path;

    /**
     * 显示排序，数值越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 部门状态：1 启用，0 停用。
     */
    private Integer enabled;
}
