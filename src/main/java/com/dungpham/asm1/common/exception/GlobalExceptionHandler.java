package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
                .map(err -> new ExceptionResponse(err.getField(), err.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(BaseResponse.build(errors, false));
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
        return buildSimpleError("INTERNAL_ERROR", "Unexpected error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private ResponseEntity<BaseResponse<ExceptionResponse>> buildSimpleError(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(BaseResponse.build(new ExceptionResponse(code, message), false));
    }
}
