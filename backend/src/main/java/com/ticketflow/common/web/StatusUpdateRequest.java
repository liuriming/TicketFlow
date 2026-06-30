package com.ticketflow.common.web;

import com.ticketflow.system.enums.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 状态更新请求 DTO。
 *
 * <p>通用配置类数据使用 {@code enabled} 表示启用或停用；用户账号使用 {@code status}
 * 表示启用或禁用。接口会按对应实体语义读取需要的字段。</p>
 *
 * @param enabled 启用状态：1 启用，0 停用。
 * @param status 用户账号状态。
 */
public record StatusUpdateRequest(
        @Min(value = 0, message = "启用状态只能是 0 或 1")
        @Max(value = 1, message = "启用状态只能是 0 或 1")
        Integer enabled,
        UserStatus status
) {
}
