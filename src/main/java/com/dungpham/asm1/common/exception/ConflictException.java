package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;

public class ConflictException extends AppException {
    public ConflictException(String item) {
        super(ErrorCode.CONFLICT, item);
        System.out.println("ConflictException created with item: " + item +
                ", formattedMessage: " + getFormattedMessage());
    }
}
