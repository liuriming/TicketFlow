package com.ticketflow.system.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 部门保存请求 DTO。
 *
 * @param parentId 父部门 ID，根部门为 0。
 * @param deptName 部门名称。
 * @param sortOrder 显示排序。
 */
public record SysDeptSaveRequest(
        Long parentId,
        @NotBlank(message = "部门名称不能为空")
        String deptName,
        Integer sortOrder
) {
}
