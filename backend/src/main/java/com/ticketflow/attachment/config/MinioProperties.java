package com.ticketflow.attachment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 配置属性。
 *
 * <p>从 application.yml 的 minio 配置段读取对象存储连接信息和默认存储桶。</p>
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 服务地址。
     */
    private String endpoint;

    /**
     * 访问密钥。
     */
    private String accessKey;

    /**
     * 访问密钥对应的 secret。
     */
    private String secretKey;

    /**
     * 默认存储桶名称。
     */
    private String bucket;
}
