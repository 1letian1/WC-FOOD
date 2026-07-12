package com.shike.ordering.common.exception;
import org.springframework.http.HttpStatus;
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(ErrorCode errorCode) { super(errorCode); }
    public ResourceNotFoundException(int code, String message) { super(code, message, HttpStatus.NOT_FOUND); }
}
