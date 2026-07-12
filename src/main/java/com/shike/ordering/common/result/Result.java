package com.shike.ordering.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shike.ordering.common.exception.ErrorCode;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record Result<T>(Integer code, String message, T data, String requestId, LocalDateTime timestamp) {
    public static <T> Result<T> success() { return success(null); }
    public static <T> Result<T> success(T data) { return success("success", data); }
    public static <T> Result<T> success(String message, T data) { return of(0, message, data); }
    public static <T> Result<T> failure(ErrorCode errorCode) { return of(errorCode.getCode(), errorCode.getMessage(), null); }
    public static <T> Result<T> failure(Integer code, String message) { return of(code, message, null); }
    private static <T> Result<T> of(Integer code, String message, T data) {
        return new Result<>(code, message, data, MDC.get("requestId"), LocalDateTime.now());
    }
}
