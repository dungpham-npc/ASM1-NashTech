package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
            UserException.class,
    })
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleCustomAppException(RuntimeException ex) {
        if (ex instanceof UserException ue) {
            return buildErrorResponse(ErrorCode.valueOf(ue.getErrorCode()), HttpStatus.BAD_REQUEST);
        } else if (ex instanceof ProductException pe) {
            return buildErrorResponse(ErrorCode.valueOf(pe.getErrorCode()), HttpStatus.BAD_REQUEST);
        }
        return buildErrorResponse("UNKNOWN_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // === Built-in Spring validation ===

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<List<ExceptionResponse>>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ExceptionResponse(err.getField(), err.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(BaseResponse.build(errors, false));
    }

    // === Common platform exceptions ===

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleBadCredentials() {
        return buildErrorResponse(ErrorCode.BAD_CREDENTIAL_LOGIN, HttpStatus.UNAUTHORIZED);
    }

//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<BaseResponse<ExceptionResponse>> handleNotFound() {
//        return buildErrorResponse(ErrorCode.RESOURCES_NOT_FOUND, HttpStatus.NOT_FOUND);
//    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleSecurity() {
        return buildErrorResponse(ErrorCode.UNAUTHORIZED_CART_ACCESS, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleInvalidState(RuntimeException ex) {
        return buildErrorResponse("INVALID_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<BaseResponse<ExceptionResponse>> handleIO(IOException ex) {
        return buildErrorResponse("IO_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // === Fallback ===

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleGenericException(Exception ex) {
        var errorResponse = BaseResponse.build("Internal Error: " + ex.getMessage(), false);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // === Helper ===

    private ResponseEntity<BaseResponse<ExceptionResponse>> buildErrorResponse(ErrorCode errorCode, HttpStatus status) {
        return buildErrorResponse(errorCode.getCode(), errorCode.getMessage(), status);
    }

    private ResponseEntity<BaseResponse<ExceptionResponse>> buildErrorResponse(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(BaseResponse.build(new ExceptionResponse(code, message), false));
    }
}
