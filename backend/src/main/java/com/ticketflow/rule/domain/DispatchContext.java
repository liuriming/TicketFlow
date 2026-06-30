package com.ticketflow.rule.domain;

/**
 * 派单上下文。
 *
 * @param ticketId 工单 ID。
 * @param deptId 工单所属部门 ID。
 * @param categoryCode 工单分类编码。
 */
public record DispatchContext(Long ticketId, Long deptId, String categoryCode) {
}
