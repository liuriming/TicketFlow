package com.ticketflow.attachment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 附件实体。
 *
 * <p>附件文件本体存储在 MinIO，数据库仅保存文件元数据和业务关联，避免数据库承担大文件存储压力。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attachment")
public class Attachment extends BaseEntity {

    /**
     * 业务类型，例如 TICKET、COMMENT。
     */
    private String businessType;

    /**
     * 业务 ID，例如工单 ID 或评论 ID。
     */
    private Long businessId;

    /**
     * 原始文件名。
     */
    private String originalName;

    /**
     * MinIO 存储桶名称。
     */
    private String bucketName;

    /**
     * MinIO 对象名称。
     */
    private String objectName;

    /**
     * 文件大小，单位字节。
     */
    private Long fileSize;

    /**
     * 文件 MIME 类型。
     */
    private String contentType;

    /**
     * 上传人用户 ID。
     */
    private Long uploaderId;
}
