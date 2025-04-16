package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;

public class ForbiddenException extends AppException {
    public ForbiddenException(String target) {
        super(ErrorCode.FORBIDDEN, target);
    }
}