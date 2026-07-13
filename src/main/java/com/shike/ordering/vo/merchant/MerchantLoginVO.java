package com.shike.ordering.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商家登录结果")
public record MerchantLoginVO(
        @Schema(description = "商家端访问 Token") String token,
        @Schema(description = "固定有效期，单位秒") long expiresInSeconds,
        @Schema(description = "商家资料") MerchantProfileVO merchant) { }
