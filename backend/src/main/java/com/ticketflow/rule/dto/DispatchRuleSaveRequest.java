package com.ticketflow.rule.dto;

/**
 * 派单规则保存请求 DTO。
 *
 * @param categoryId 工单分类 ID。
 * @param deptId 适用部门 ID，为空表示全部部门。
 * @param skillCode 技能或负责范围编码。
 * @param assigneeId 默认处理人用户 ID。
 * @param priority 规则优先级，数值越大越优先。
 */
public record DispatchRuleSaveRequest(
        Long categoryId,
        Long deptId,
        String skillCode,
        Long assigneeId,
        Integer priority
) {
}
