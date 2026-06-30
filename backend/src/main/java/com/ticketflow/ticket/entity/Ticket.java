package com.ticketflow.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ticketflow.common.domain.BaseEntity;
import com.ticketflow.ticket.enums.TicketPriority;
import com.ticketflow.ticket.enums.TicketStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 工单实体。
 *
 * <p>工单记录员工提交的 IT 运维报修诉求，贯穿创建、派单、接单、处理、确认和关闭全流程。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ticket")
public class Ticket extends BaseEntity {

    /**
     * 工单编号，用于界面展示和检索。
     */
    private String ticketNo;

    /**
     * 工单标题。
     */
    private String title;

    /**
     * 工单详细描述。
     */
    private String description;

    /**
     * 工单分类 ID。
     */
    private Long categoryId;

    /**
     * 工单优先级。
     */
    private TicketPriority priority;

    /**
     * 当前工单状态。
     */
    private TicketStatus status;

    /**
     * 创建人用户 ID。
     */
    private Long creatorId;

    /**
     * 创建人部门 ID。
     */
    private Long creatorDeptId;

    /**
     * 当前处理人用户 ID。
     */
    private Long assigneeId;

    /**
     * SLA 响应截止时间。
     */
    private LocalDateTime responseDeadline;

    /**
     * SLA 处理截止时间。
     */
    private LocalDateTime resolveDeadline;

    /**
     * 实际响应时间。
     */
    private LocalDateTime respondedAt;

    /**
     * 实际关闭时间。
     */
    private LocalDateTime closedAt;
}
