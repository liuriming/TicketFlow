package com.ticketflow.rule.domain;

/**
 * 自动派单待匹配工单。
 *
 * @param ticketId 工单 ID。
 * @param categoryId 工单分类 ID。
 * @param deptId 工单创建人所属部门 ID。
 * @param categoryCode 工单分类编码。
 */
public record AutoDispatchTicket(Long ticketId, Long categoryId, Long deptId, String categoryCode) {
}
