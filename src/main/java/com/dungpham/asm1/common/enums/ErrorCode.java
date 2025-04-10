package com.dungpham.asm1.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    USER_NOT_FOUND("1000", "Can not find user with email"),
    USER_IS_DEACTIVATED("1001", "Your account is deactivated"),
    BAD_CREDENTIAL_LOGIN("1002", "Invalid username or password"),
    RESOURCES_NOT_FOUND("1003", "Can not find resources"),
    PHONE_AND_MAIL_EXIST("1004", "Both phone number and email already exist"),
    EMAIL_EXIST("1005", "Email already exists"),
    PHONE_EXIST("1006", "Phone number already exists"),
    UNAUTHORIZED_CART_ACCESS("1007", "Cannot access cart that doesn't belong to current user"),
    OTP_INVALID_OR_EXPIRED("1008", "Your Code invalid or expired"),
    OTP_NOT_MATCH("1009", "Your Code does not match");

    private final String code;
    private final String message;
}
