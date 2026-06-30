package com.ticketflow.common.exception;

/**
 * 业务异常。
 *
 * <p>用于表达可预期的业务失败，例如登录失败、数据不存在、状态流转不合法等。
 * 全局异常处理器会将该异常转换为统一 JSON 响应。</p>
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
