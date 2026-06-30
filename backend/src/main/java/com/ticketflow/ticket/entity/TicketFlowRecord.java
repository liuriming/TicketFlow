package com.ticketflow.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import com.ticketflow.ticket.enums.TicketStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单流转记录实体。
 *
 * <p>记录每次状态变更、转派、驳回、关闭等关键操作，用于审计和详情页时间线展示。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ticket_flow_record")
public class TicketFlowRecord extends BaseEntity {

    /**
     * 工单 ID。
     */
    private Long ticketId;

    /**
     * 操作前状态。
     */
    private TicketStatus fromStatus;

    /**
     * 操作后状态。
     */
    private TicketStatus toStatus;

    /**
     * 操作人用户 ID。
     */
    private Long operatorId;

    /**
     * 操作说明。
     */
    private String remark;
}
