package com.ticketflow.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单评论实体。
 *
 * <p>用于员工和运维人员在工单处理过程中补充信息、沟通处理结果和记录协作内容。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ticket_comment")
public class TicketComment extends BaseEntity {

    /**
     * 工单 ID。
     */
    private Long ticketId;

    /**
     * 评论人用户 ID。
     */
    private Long userId;

    /**
     * 评论内容。
     */
    private String content;

    /**
     * 是否内部备注：1 内部可见，0 工单参与人可见。
     */
    private Integer internalOnly;
}
