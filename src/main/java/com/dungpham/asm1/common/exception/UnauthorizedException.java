package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;

public class UnauthorizedException extends AppException {
    public UnauthorizedException(String target) {
        super(ErrorCode.UNAUTHORIZED, target);
    }
}