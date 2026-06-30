package com.ticketflow.attachment.dto;

import java.io.InputStream;

/**
 * 附件下载 DTO。
 *
 * @param inputStream 文件输入流。
 * @param originalName 原始文件名。
 * @param contentType 文件 MIME 类型。
 * @param fileSize 文件大小，单位字节。
 */
public record AttachmentDownload(
        InputStream inputStream,
        String originalName,
        String contentType,
        Long fileSize
) {
}
