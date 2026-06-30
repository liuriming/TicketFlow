package com.ticketflow.rule.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 工单分类保存请求 DTO。
 *
 * @param parentId 父分类 ID，根分类为 0。
 * @param categoryName 分类名称，例如网络故障、账号权限。
 * @param categoryCode 分类编码，用于派单规则匹配。
 * @param sortOrder 显示排序。
 */
public record TicketCategorySaveRequest(
        Long parentId,
        @NotBlank(message = "分类名称不能为空")
        String categoryName,
        @NotBlank(message = "分类编码不能为空")
        String categoryCode,
        Integer sortOrder
) {
}
