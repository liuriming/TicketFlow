package com.ticketflow.common.web;

import com.ticketflow.common.exception.ErrorCode;

/**
 * 后端接口统一响应结构。
 *
 * @param code 业务状态码，0 表示成功，非 0 表示失败。
 * @param message 中文提示信息，用于前端直接展示。
 * @param data 响应数据主体，按具体接口返回不同 DTO。
 * @param <T> 响应数据类型。
 */
public record ApiResult<T>(int code, String message, T data) {

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.message(), data);
    }

    public static ApiResult<Void> success() {
        return new ApiResult<>(ErrorCode.SUCCESS.code(), ErrorCode.SUCCESS.message(), null);
    }

    public static ApiResult<Void> fail(ErrorCode errorCode, String message) {
        return new ApiResult<>(errorCode.code(), message, null);
    }
}
