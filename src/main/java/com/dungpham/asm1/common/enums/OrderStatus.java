package com.dungpham.asm1.common.enums;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus fromString(String status) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.name().equalsIgnoreCase(status)) {
                return orderStatus;
            }
        }
        throw new IllegalArgumentException("Unknown order status: " + status);
    }
}
