package com.shike.ordering.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BusinessStatus {
    CLOSED(0, "休息中"),
    OPEN(1, "营业中");

    private final int code;
    private final String description;

    BusinessStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BusinessStatus fromCode(int code) {
        return Arrays.stream(values()).filter(status -> status.code == code).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown business status code"));
    }
}
