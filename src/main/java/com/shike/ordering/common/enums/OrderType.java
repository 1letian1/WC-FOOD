package com.shike.ordering.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum OrderType {
    DINE_IN(1, "堂食"),
    DELIVERY(2, "配送");

    private final int code;
    private final String description;

    OrderType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown order type code"));
    }
}
