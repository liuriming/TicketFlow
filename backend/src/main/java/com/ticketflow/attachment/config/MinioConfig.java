package com.ticketflow.attachment.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置。
 *
 * <p>统一创建 MinioClient，附件服务通过该客户端上传、下载和删除文件。</p>
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
