package com.dungpham.asm1.common.enums;

public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED;

    public static TransactionStatus fromString(String status) {
        for (TransactionStatus transactionStatus : TransactionStatus.values()) {
            if (transactionStatus.name().equalsIgnoreCase(status)) {
                return transactionStatus;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
}
