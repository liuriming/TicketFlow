package com.ticketflow.attachment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketflow.attachment.dto.AttachmentBindRequest;
import com.ticketflow.attachment.dto.AttachmentDownload;
import com.ticketflow.attachment.dto.AttachmentResponse;
import com.ticketflow.attachment.entity.Attachment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 附件服务接口。
 *
 * <p>封装附件上传、下载、删除、绑定业务和按业务查询附件列表。</p>
 */
public interface AttachmentService extends IService<Attachment> {

    /**
     * 上传附件。
     *
     * @param file 上传文件。
     * @param businessType 业务类型，可为空，空值表示临时附件。
     * @param businessId 业务 ID，可为空。
     * @return 附件元数据。
     */
    AttachmentResponse upload(MultipartFile file, String businessType, Long businessId);

    /**
     * 下载附件。
     *
     * @param id 附件 ID。
     * @return 附件下载信息。
     */
    AttachmentDownload download(Long id);

    /**
     * 绑定附件到业务对象。
     *
     * @param id 附件 ID。
     * @param request 绑定请求。
     * @return 附件元数据。
     */
    AttachmentResponse bind(Long id, AttachmentBindRequest request);

    /**
     * 删除附件。
     *
     * @param id 附件 ID。
     */
    void deleteAttachment(Long id);

    /**
     * 按业务对象查询附件。
     *
     * @param businessType 业务类型。
     * @param businessId 业务 ID。
     * @return 附件列表。
     */
    List<AttachmentResponse> listByBusiness(String businessType, Long businessId);
}
