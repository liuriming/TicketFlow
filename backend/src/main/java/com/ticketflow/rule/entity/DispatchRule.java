package com.ticketflow.rule.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 派单规则实体。
 *
 * <p>用于配置分类、部门、技能范围和默认处理人等自动派单条件。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_rule")
public class DispatchRule extends BaseEntity {

    /**
     * 工单分类 ID。
     */
    private Long categoryId;

    /**
     * 适用部门 ID，为空表示全部部门。
     */
    private Long deptId;

    /**
     * 技能或负责范围编码。
     */
    private String skillCode;

    /**
     * 默认处理人用户 ID。
     */
    private Long assigneeId;

    /**
     * 规则优先级，数值越大优先级越高。
     */
    private Integer priority;

    /**
     * 是否启用：1 启用，0 停用。
     */
    private Integer enabled;
}
