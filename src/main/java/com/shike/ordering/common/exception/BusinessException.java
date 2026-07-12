package com.shike.ordering.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final Integer code;
    private final HttpStatus httpStatus;
    public BusinessException(ErrorCode errorCode) { this(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus()); }
    public BusinessException(Integer code, String message, HttpStatus httpStatus) { super(message); this.code = code; this.httpStatus = httpStatus; }
    public Integer getCode() { return code; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
