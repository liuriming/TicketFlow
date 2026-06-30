package com.ticketflow.common.exception;

/**
 * 统一错误码。
 *
 * <p>错误码用于前端稳定识别错误类型，错误消息用于中文界面展示。</p>
 */
public enum ErrorCode {

    /**
     * 请求成功。
     */
    SUCCESS(0, "操作成功"),

    /**
     * 请求参数不合法。
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 用户未登录或登录态过期。
     */
    UNAUTHORIZED(401, "登录状态已失效"),

    /**
     * 用户没有访问目标资源的权限。
     */
    FORBIDDEN(403, "没有操作权限"),

    /**
     * 请求的数据不存在。
     */
    NOT_FOUND(404, "数据不存在"),

    /**
     * 当前业务状态不允许执行该操作。
     */
    BUSINESS_ERROR(409, "业务处理失败"),

    /**
     * 服务端未预期异常。
     */
    INTERNAL_ERROR(500, "系统异常");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
