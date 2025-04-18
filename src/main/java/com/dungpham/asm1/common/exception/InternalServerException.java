package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;

public class InternalServerException extends AppException {
    public InternalServerException(String message) {
        super(ErrorCode.INTERNAL_ERROR, message);
    }
}
