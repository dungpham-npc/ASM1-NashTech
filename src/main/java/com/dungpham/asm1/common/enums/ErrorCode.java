package com.dungpham.asm1.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode { //TODO: Refactor this to use a common error code structure, for example, NOT_FOUND exception will be "%s_NOT_FOUND" and so on
    USER_NOT_FOUND("1000", "Can not find user with email"),
    USER_IS_DEACTIVATED("1001", "Your account is deactivated"),
    BAD_CREDENTIAL_LOGIN("1002", "Invalid username or password"),
    RESOURCES_NOT_FOUND("1003", "Can not find resources"),
    PHONE_AND_MAIL_EXIST("1004", "Both phone number and email already exist"),
    EMAIL_EXIST("1005", "Email already exists"),
    PHONE_EXIST("1006", "Phone number already exists"),
    UNAUTHORIZED_CART_ACCESS("1007", "Cannot access cart that doesn't belong to current user"),
    OTP_INVALID_OR_EXPIRED("1008", "Your Code invalid or expired"),
    OTP_NOT_MATCH("1009", "Your Code does not match"),
    PRODUCT_ID_IS_NOT_NULL("1010", "Product id is not null"),
    PRICE_SMALLER_THAN_ZERO("1011", "Price must be greater than 0"),
    PRODUCT_NAME_EMPTY("1012", "Product name cannot be empty"),
    PRODUCT_DESCRIPTION_EMPTY("1013", "Product description cannot be empty"),
    PRODUCT_NOT_FOUND("1014", "Product not found"),
    CATEGORY_NOT_FOUND("1015", "Category not found"),
    SECURITY_ERROR("1016", "User is not authorized or token is invalid"),
    PRODUCT_RATING_OUT_OF_BOUNDS("1017", "Rating must be between 1 and 5"),
    PRODUCT_ALREADY_RATED("1018", "Product already rated by user");

    private final String code;
    private final String message;
}
