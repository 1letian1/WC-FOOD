package com.shike.ordering.common.exception;

import com.shike.ordering.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException exception) {
        return response(exception.getHttpStatus(), exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getDefaultMessage()).orElse(ErrorCode.PARAM_ERROR.getMessage());
        return response(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getDefaultMessage()).orElse(ErrorCode.PARAM_ERROR.getMessage());
        return response(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream().findFirst()
                .map(violation -> violation.getMessage()).orElse(ErrorCode.PARAM_ERROR.getMessage());
        return response(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Result<Void>> handleBadRequest(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.REQUEST_FORMAT_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethod(HttpRequestMethodNotSupportedException exception) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_SUPPORTED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMediaType(HttpMediaTypeNotSupportedException exception) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCode.MEDIA_TYPE_NOT_SUPPORTED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleUploadTooLarge(MaxUploadSizeExceededException exception) {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.FILE_TOO_LARGE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoResourceFoundException exception) {
        return response(HttpStatus.NOT_FOUND, ErrorCode.API_NOT_FOUND);
    }

    @ExceptionHandler({DuplicateKeyException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Result<Void>> handleDatabase(Exception exception) {
        log.warn("database constraint violation, type={}", exception.getClass().getSimpleName());
        return response(HttpStatus.CONFLICT, ErrorCode.DATA_CONFLICT);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<Result<Void>> handleRedis(RedisConnectionFailureException exception) {
        log.error("redis service unavailable", exception);
        return response(HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.REDIS_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception exception, HttpServletRequest request) {
        log.error("unhandled exception, method={}, uri={}", request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SYSTEM_ERROR);
    }

    private ResponseEntity<Result<Void>> response(HttpStatus status, ErrorCode errorCode) {
        return response(status, errorCode.getCode(), errorCode.getMessage());
    }

    private ResponseEntity<Result<Void>> response(HttpStatus status, Integer code, String message) {
        return ResponseEntity.status(status).body(Result.failure(code, message));
    }
}
