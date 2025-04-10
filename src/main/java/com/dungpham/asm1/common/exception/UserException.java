package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserException extends RuntimeException {
    private final String errorCode;
    private final String message;

    public UserException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
