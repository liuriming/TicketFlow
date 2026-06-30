package com.ticketflow.ticket.enums;

/**
 * 工单优先级。
 *
 * <p>优先级会影响 SLA 响应时限、处理时限和报表统计。</p>
 */
public enum TicketPriority {

    /**
     * 低优先级，普通咨询或轻微影响。
     */
    LOW,

    /**
     * 中优先级，影响单个用户或普通办公流程。
     */
    MEDIUM,

    /**
     * 高优先级，影响部门业务或多人协作。
     */
    HIGH,

    /**
     * 紧急优先级，影响核心系统或大范围业务。
     */
    URGENT
}
