package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;

public class NotFoundException extends AppException {
    public NotFoundException(String target) {
        super(ErrorCode.NOT_FOUND, target);
        System.out.println("NotFoundException created with target: " + target +
                ", formattedMessage: " + getFormattedMessage());
    }
}
