package com.ticketflow.attachment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 附件绑定业务请求 DTO。
 *
 * @param businessType 业务类型，例如 TICKET、COMMENT。
 * @param businessId 业务 ID，例如工单 ID 或评论 ID。
 */
public record AttachmentBindRequest(
        @NotBlank(message = "业务类型不能为空")
        String businessType,
        @NotNull(message = "业务 ID 不能为空")
        Long businessId
) {
}
