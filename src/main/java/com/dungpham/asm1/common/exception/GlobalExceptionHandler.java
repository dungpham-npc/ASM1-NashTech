package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseResponse<Object>> handleApiException(AppException ex) {
        ErrorCode code = ex.getErrorCode();
        BaseResponse<Object> response = new BaseResponse<>();
        response.setStatus(false);
        response.setCode(code.getCode());
        response.setMessage(ex.getFormattedMessage());
        log.error("AppException: {} with message: {}", code.getCode(), ex.getFormattedMessage());
        response.setData(null);
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<List<ExceptionResponse>>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ExceptionResponse(null, err.getField() + " - " + err.getDefaultMessage()))
                .toList();
        return buildSimpleError(ErrorCode.VALIDATION_ERROR, "Validation failed", HttpStatus.BAD_REQUEST, errors, true);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleBadCredentials(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.BAD_CREDENTIAL_LOGIN, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleSecurity(SecurityException ex) {
        log.error("Security exception: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.UNAUTHORIZED_ACCESS, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleInvalidState(RuntimeException ex) {
        return buildSimpleError(ErrorCode.INVALID_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleIO(IOException ex) {
        log.error("IO exception: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleNullPointer(NullPointerException ex) {
        log.error("Null pointer exception: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.INTERNAL_ERROR, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleAccessDenied(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleInternalServer(InternalServerException ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleRedisConnection(RedisConnectionFailureException ex) {
        log.error("Redis connection failure: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<List<ExceptionResponse>>> handleBindException(BindException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ExceptionResponse(null, err.getField() + " - " + err.getDefaultMessage()))
                .toList();
        return buildSimpleError(ErrorCode.VALIDATION_ERROR, "Validation failed", HttpStatus.BAD_REQUEST, errors, true);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
        log.error("Internal authentication service exception: {}", ex.getMessage(), ex);
        return buildSimpleError(ErrorCode.AUTHENTICATION_SERVICE_ERROR, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<BaseResponse<ExceptionResponse>> buildSimpleError(ErrorCode errorCode, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(BaseResponse.build(new ExceptionResponse(errorCode.getCode(), errorCode.formatMessage()), false));
    }

    private ResponseEntity<BaseResponse<ExceptionResponse>> buildSimpleError(ErrorCode errorCode, String arg, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(BaseResponse.build(new ExceptionResponse(errorCode.getCode(), errorCode.formatMessage(arg)), false));
    }

    private <T> ResponseEntity<BaseResponse<T>> buildSimpleError(ErrorCode errorCode, String message, HttpStatus status, T data, boolean overrideResponseFields) {
        BaseResponse<T> response = BaseResponse.build(data, false);
        if (overrideResponseFields) {
            response.setCode(errorCode.getCode());
            response.setMessage(errorCode.formatMessage(message));
        }
        return ResponseEntity.status(status).body(response);
    }
}