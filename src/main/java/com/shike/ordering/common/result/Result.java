package com.shike.ordering.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shike.ordering.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "统一接口响应")
public record Result<T>(
        @Schema(description = "业务错误码，0表示成功", example = "0") Integer code,
        @Schema(description = "稳定的响应信息", example = "success") String message,
        @Schema(description = "业务数据；无内容时为null") T data,
        @Schema(description = "请求追踪ID", example = "8c8b9c27bafd4d6f") String requestId,
        @Schema(description = "服务端响应时间") LocalDateTime timestamp) {
    public static <T> Result<T> success() { return success(null); }
    public static <T> Result<T> success(T data) { return success("success", data); }
    public static <T> Result<T> success(String message, T data) { return of(0, message, data); }
    public static <T> Result<T> failure(ErrorCode errorCode) { return of(errorCode.getCode(), errorCode.getMessage(), null); }
    public static <T> Result<T> failure(Integer code, String message) { return of(code, message, null); }
    private static <T> Result<T> of(Integer code, String message, T data) {
        return new Result<>(code, message, data, MDC.get("requestId"), LocalDateTime.now());
    }
}
