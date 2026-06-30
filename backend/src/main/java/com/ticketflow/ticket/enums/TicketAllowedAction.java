package com.ticketflow.ticket.enums;

/**
 * 当前用户对工单可执行的动作。
 *
 * <p>后端根据工单状态、当前用户身份和权限标识计算该集合，前端只根据该集合展示操作按钮，
 * 避免页面重复维护一套状态判断规则。</p>
 */
public enum TicketAllowedAction {

    /**
     * 接单。
     */
    ACCEPT,

    /**
     * 提交处理结果。
     */
    PROCESS,

    /**
     * 转派给其他处理人。
     */
    TRANSFER,

    /**
     * 确认处理结果并关闭工单。
     */
    CONFIRM_CLOSE,

    /**
     * 驳回处理结果。
     */
    REJECT,

    /**
     * 取消工单。
     */
    CANCEL,

    /**
     * 发表评论或内部备注。
     */
    COMMENT,

    /**
     * 上传工单附件。
     */
    UPLOAD_ATTACHMENT
}
