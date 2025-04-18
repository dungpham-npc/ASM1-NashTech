package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;

public class ValidationException extends AppException {
    public ValidationException(String detail) {
        super(ErrorCode.VALIDATION_ERROR, detail);
    }
}
