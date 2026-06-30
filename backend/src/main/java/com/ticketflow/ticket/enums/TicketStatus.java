package com.ticketflow.ticket.enums;

/**
 * 工单状态。
 *
 * <p>覆盖员工提交、自动/人工分派、运维处理、员工确认和终态关闭的完整生命周期。</p>
 */
public enum TicketStatus {

    /**
     * 已创建，尚未进入派单队列。
     */
    CREATED,

    /**
     * 待分派，等待自动派单或主管人工分派。
     */
    PENDING_ASSIGN,

    /**
     * 待接单，已经分派给运维人员但尚未接单。
     */
    PENDING_ACCEPT,

    /**
     * 处理中，运维人员已经接单并开始处理。
     */
    PROCESSING,

    /**
     * 待确认，运维提交处理结果后等待员工确认。
     */
    PENDING_CONFIRM,

    /**
     * 已关闭，员工确认或系统自动关闭后的终态。
     */
    CLOSED,

    /**
     * 已驳回，员工认为处理结果不符合要求。
     */
    REJECTED,

    /**
     * 已取消，创建人或管理员取消工单后的终态。
     */
    CANCELED
}
