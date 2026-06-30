package com.ticketflow.system.enums;

/**
 * 用户状态。
 *
 * <p>禁用用户不能登录系统，但历史工单和操作日志仍保留其用户 ID。</p>
 */
public enum UserStatus {

    /**
     * 正常启用。
     */
    ENABLED,

    /**
     * 已禁用。
     */
    DISABLED
}
