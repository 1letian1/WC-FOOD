package com.shike.ordering.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum OrderStatus {
    PENDING_ACCEPT(1, "待接单"),
    ACCEPTED(2, "已接单"),
    COOKING(3, "制作中"),
    READY_FOR_PICKUP(4, "待取餐"),
    DELIVERING(5, "配送中"),
    DELIVERED(6, "已送达"),
    COMPLETED(7, "已完成"),
    CANCELLED(8, "已取消"),
    REJECTED(9, "已拒单");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown order status code"));
    }
}
