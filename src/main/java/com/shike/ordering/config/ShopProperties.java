package com.shike.ordering.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("shike.shop")
public record ShopProperties(
        @NotNull(message = "默认店铺ID不能为空")
        @Positive(message = "默认店铺ID必须为正数")
        Long defaultShopId) {
}
