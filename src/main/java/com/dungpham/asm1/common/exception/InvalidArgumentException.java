package com.dungpham.asm1.common.exception;

import com.dungpham.asm1.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidArgumentException extends AppException{
    public InvalidArgumentException(String argumentName, String reason) {
        super(ErrorCode.INVALID_ARGUMENT, argumentName, reason);
        log.info("InvalidArgumentException instantiated: argumentName={}, reason={}, formattedMessage={}, super.getMessage()={}",
                argumentName, reason, getFormattedMessage(), super.getMessage());
        System.err.println("[InvalidArgumentException] instantiated: argumentName=" + argumentName +
                ", reason=" + reason + ", formattedMessage=" + getFormattedMessage());
    }
}
