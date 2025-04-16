package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class InvalidTokenException extends AppException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
