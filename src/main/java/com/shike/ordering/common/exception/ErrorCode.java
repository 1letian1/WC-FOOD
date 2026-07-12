package com.shike.ordering.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    SUCCESS(0, "success", HttpStatus.OK),
    PARAM_ERROR(10001, "参数错误", HttpStatus.BAD_REQUEST),
    REQUEST_FORMAT_ERROR(10002, "请求格式错误", HttpStatus.BAD_REQUEST),
    METHOD_NOT_SUPPORTED(10003, "请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED),
    API_NOT_FOUND(10004, "接口不存在", HttpStatus.NOT_FOUND),
    MEDIA_TYPE_NOT_SUPPORTED(10005, "Content-Type 不支持", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    DATA_CONFLICT(10006, "数据已存在或存在关联，无法操作", HttpStatus.CONFLICT),
    UNAUTHORIZED(20001, "未登录或登录已过期", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(20002, "无权限访问", HttpStatus.FORBIDDEN),
    ORDER_NOT_FOUND(60001, "订单不存在", HttpStatus.NOT_FOUND),
    ORDER_STATE_CONFLICT(60002, "订单状态已变化，请刷新后重试", HttpStatus.CONFLICT),
    DUPLICATE_OPERATION(60004, "请求重复，请勿重复操作", HttpStatus.CONFLICT),
    REDIS_UNAVAILABLE(90002, "服务暂时不可用，请稍后重试", HttpStatus.SERVICE_UNAVAILABLE),
    SYSTEM_ERROR(99999, "系统繁忙，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
