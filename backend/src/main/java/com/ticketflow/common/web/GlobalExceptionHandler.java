package com.ticketflow.common.web;

import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>将参数校验异常、业务异常和系统异常统一转换为标准 JSON 响应，
 * 避免接口向前端泄露 Java 堆栈信息。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusinessException(BusinessException exception) {
        return ApiResult.fail(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResult<Void> handleBadRequest(Exception exception) {
        return ApiResult.fail(ErrorCode.BAD_REQUEST, "请求参数错误：" + exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception exception) {
        return ApiResult.fail(ErrorCode.INTERNAL_ERROR, "系统异常，请联系管理员");
    }
}
