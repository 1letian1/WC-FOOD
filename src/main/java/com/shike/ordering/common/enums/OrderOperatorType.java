package com.shike.ordering.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum OrderOperatorType {
    USER("USER", "用户"),
    MERCHANT("MERCHANT", "商家"),
    SYSTEM("SYSTEM", "系统");

    private final String code;
    private final String description;

    OrderOperatorType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderOperatorType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown order operator type code"));
    }
}
