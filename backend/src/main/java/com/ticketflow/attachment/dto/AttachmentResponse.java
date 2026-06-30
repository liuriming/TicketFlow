package com.ticketflow.attachment.dto;

import java.time.LocalDateTime;

/**
 * 附件响应 DTO。
 *
 * @param id 附件 ID。
 * @param businessType 业务类型。
 * @param businessId 业务 ID。
 * @param originalName 原始文件名。
 * @param bucketName 存储桶名称。
 * @param objectName 对象名称。
 * @param fileSize 文件大小，单位字节。
 * @param contentType 文件 MIME 类型。
 * @param uploaderId 上传人用户 ID。
 * @param createdAt 上传时间。
 */
public record AttachmentResponse(
        Long id,
        String businessType,
        Long businessId,
        String originalName,
        String bucketName,
        String objectName,
        Long fileSize,
        String contentType,
        Long uploaderId,
        LocalDateTime createdAt
) {
}
