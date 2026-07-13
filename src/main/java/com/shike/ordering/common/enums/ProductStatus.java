package com.shike.ordering.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ProductStatus {
    OFF_SALE(0, "已下架"),
    ON_SALE(1, "销售中"),
    SOLD_OUT(2, "已售罄");

    private final int code;
    private final String description;

    ProductStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProductStatus fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product status code"));
    }
}
