package com.ticketflow.attachment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ticketflow.attachment.config.MinioProperties;
import com.ticketflow.attachment.domain.AttachmentBusinessAccessValidator;
import com.ticketflow.attachment.domain.AttachmentObjectNameBuilder;
import com.ticketflow.attachment.dto.AttachmentBindRequest;
import com.ticketflow.attachment.dto.AttachmentDownload;
import com.ticketflow.attachment.dto.AttachmentResponse;
import com.ticketflow.attachment.entity.Attachment;
import com.ticketflow.attachment.mapper.AttachmentMapper;
import com.ticketflow.attachment.service.AttachmentService;
import com.ticketflow.common.context.CurrentUserContext;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 附件服务实现类。
 *
 * <p>负责将文件内容写入 MinIO，并将文件元数据保存到数据库。
 * 数据库只保存存储桶、对象名称、原始文件名、大小、MIME 类型和业务关联信息。</p>
 */
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements AttachmentService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final AttachmentBusinessAccessValidator businessAccessValidator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttachmentResponse upload(MultipartFile file, String businessType, Long businessId) {
        try {
            String targetBusinessType = StringUtils.hasText(businessType) ? businessType : "TEMP";
            businessAccessValidator.validateBusinessAccess(targetBusinessType, businessId);
            ensureBucket();
            String objectName = AttachmentObjectNameBuilder.build(
                    file.getOriginalFilename(),
                    LocalDate.now(),
                    () -> UUID.randomUUID().toString().replace("-", "")
            );
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            Attachment attachment = new Attachment();
            attachment.setBusinessType(targetBusinessType);
            attachment.setBusinessId(businessId);
            attachment.setOriginalName(file.getOriginalFilename());
            attachment.setBucketName(minioProperties.getBucket());
            attachment.setObjectName(objectName);
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setUploaderId(CurrentUserContext.getRequired().userId());
            save(attachment);
            return toResponse(attachment);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "附件上传失败");
        }
    }

    @Override
    public AttachmentDownload download(Long id) {
        try {
            Attachment attachment = getAttachmentRequired(id);
            businessAccessValidator.validateBusinessAccess(attachment.getBusinessType(), attachment.getBusinessId());
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(attachment.getBucketName())
                    .object(attachment.getObjectName())
                    .build());
            return new AttachmentDownload(
                    inputStream,
                    attachment.getOriginalName(),
                    attachment.getContentType(),
                    attachment.getFileSize()
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "附件下载失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttachmentResponse bind(Long id, AttachmentBindRequest request) {
        Attachment attachment = getAttachmentRequired(id);
        businessAccessValidator.validateBusinessAccess(attachment.getBusinessType(), attachment.getBusinessId());
        businessAccessValidator.validateBusinessAccess(request.businessType(), request.businessId());
        attachment.setBusinessType(request.businessType());
        attachment.setBusinessId(request.businessId());
        updateById(attachment);
        return toResponse(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long id) {
        try {
            Attachment attachment = getAttachmentRequired(id);
            businessAccessValidator.validateBusinessAccess(attachment.getBusinessType(), attachment.getBusinessId());
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(attachment.getBucketName())
                    .object(attachment.getObjectName())
                    .build());
            removeById(id);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "附件删除失败");
        }
    }

    @Override
    public List<AttachmentResponse> listByBusiness(String businessType, Long businessId) {
        businessAccessValidator.validateListAccess(businessType, businessId);
        return list(Wrappers.<Attachment>lambdaQuery()
                        .eq(StringUtils.hasText(businessType), Attachment::getBusinessType, businessType)
                        .eq(businessId != null, Attachment::getBusinessId, businessId)
                        .orderByDesc(Attachment::getCreatedAt))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());
        }
    }

    private Attachment getAttachmentRequired(Long id) {
        Attachment attachment = getById(id);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "附件不存在");
        }
        return attachment;
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getBusinessType(),
                attachment.getBusinessId(),
                attachment.getOriginalName(),
                attachment.getBucketName(),
                attachment.getObjectName(),
                attachment.getFileSize(),
                attachment.getContentType(),
                attachment.getUploaderId(),
                attachment.getCreatedAt()
        );
    }
}
