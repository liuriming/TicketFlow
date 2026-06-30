package com.ticketflow.attachment.controller;

import com.ticketflow.attachment.dto.AttachmentBindRequest;
import com.ticketflow.attachment.dto.AttachmentDownload;
import com.ticketflow.attachment.dto.AttachmentResponse;
import com.ticketflow.attachment.service.AttachmentService;
import com.ticketflow.common.web.ApiResult;
import com.ticketflow.system.annotation.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 附件接口。
 *
 * <p>提供上传、下载、删除、绑定业务对象和按业务查询附件列表能力。</p>
 */
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@RequirePermission("ticket:list")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping
    public ApiResult<List<AttachmentResponse>> list(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Long businessId
    ) {
        return ApiResult.success(attachmentService.listByBusiness(businessType, businessId));
    }

    @PostMapping("/upload")
    public ApiResult<AttachmentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Long businessId
    ) {
        return ApiResult.success(attachmentService.upload(file, businessType, businessId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        AttachmentDownload download = attachmentService.download(id);
        String encodedName = URLEncoder.encode(download.originalName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType() == null ? "application/octet-stream" : download.contentType()))
                .contentLength(download.fileSize() == null ? -1 : download.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(new InputStreamResource(download.inputStream()));
    }

    @PutMapping("/{id}/bind")
    public ApiResult<AttachmentResponse> bind(
            @PathVariable Long id,
            @Valid @RequestBody AttachmentBindRequest request
    ) {
        return ApiResult.success(attachmentService.bind(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ApiResult.success();
    }
}
