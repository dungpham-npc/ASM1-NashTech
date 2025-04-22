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
        return buildSimpleError("VALIDATION_ERROR", "Validation failed", HttpStatus.BAD_REQUEST, errors, true);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleBadCredentials() {
        return buildSimpleError("BAD_CREDENTIAL_LOGIN", "Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleSecurity() {
        return buildSimpleError("UNAUTHORIZED_ACCESS", "Access denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleInvalidState(RuntimeException ex) {
        return buildSimpleError("INVALID_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleIO(IOException ex) {
        return buildSimpleError("IO_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleNullPointer(NullPointerException ex) {
        ex.printStackTrace();
        return buildSimpleError("NULL_POINTER_EXCEPTION", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildSimpleError("INTERNAL_ERROR", "Error happened while processing your request, contact admin for more info", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleAccessDenied(AccessDeniedException ex) {
        return buildSimpleError(
                "ACCESS_DENIED",
                "You don't have permission to access this resource",
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleInternalServer(InternalServerException ex) {
        return buildSimpleError(
                "INTERNAL_ERROR",
                "Please contact admin for more info",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleRedisConnection(RedisConnectionFailureException ex) {
        log.error("Redis connection failure: {}", ex.getMessage(), ex);
        return buildSimpleError(
                "REDIS_CONNECTION_ERROR",
                "Service temporarily unavailable. Please try again later.",
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<List<ExceptionResponse>>> handleBindException(org.springframework.validation.BindException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ExceptionResponse(null, err.getField() + " - " + err.getDefaultMessage()))
                .toList();
        return buildSimpleError("VALIDATION_ERROR", "Validation failed", HttpStatus.BAD_REQUEST, errors, true);
    }

    private ResponseEntity<BaseResponse<ExceptionResponse>> buildSimpleError(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(BaseResponse.build(new ExceptionResponse(code, message), false));
    }

    private <T> ResponseEntity<BaseResponse<T>> buildSimpleError(String code, String message, HttpStatus status, T data, boolean overrideResponseFields) {
        BaseResponse<T> response = BaseResponse.build(data, false);
        if (overrideResponseFields) {
            response.setCode(code);
            response.setMessage(message);
        }
        return ResponseEntity.status(status).body(response);
    }


}