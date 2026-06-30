package com.ticketflow.attachment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketflow.attachment.entity.Attachment;

/**
 * 附件 Mapper。
 *
 * <p>负责附件元数据的基础增删改查，文件内容由 MinIO 保存。</p>
 */
public interface AttachmentMapper extends BaseMapper<Attachment> {
}
