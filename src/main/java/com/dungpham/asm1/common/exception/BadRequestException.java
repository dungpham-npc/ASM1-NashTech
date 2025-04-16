package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;

public class BadRequestException extends AppException{
    public BadRequestException(String what) {
        super(ErrorCode.BAD_REQUEST, what);
    }
}
