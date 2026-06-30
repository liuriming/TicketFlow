package com.ticketflow.rule.domain;

/**
 * 自动派单规则快照。
 *
 * @param ruleId 规则 ID。
 * @param categoryId 适用工单分类 ID。
 * @param deptId 适用部门 ID。
 * @param skillCode 技能或负责范围编码。
 * @param assigneeId 候选处理人用户 ID。
 * @param priority 规则优先级。
 */
public record AutoDispatchRule(
        Long ruleId,
        Long categoryId,
        Long deptId,
        String skillCode,
        Long assigneeId,
        Integer priority
) {
}
