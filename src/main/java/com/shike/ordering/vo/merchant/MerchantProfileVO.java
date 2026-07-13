package com.shike.ordering.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "商家资料")
public record MerchantProfileVO(
        @Schema(description = "商家 ID") Long id,
        @Schema(description = "所属店铺 ID") Long shopId,
        @Schema(description = "商家账号") String username,
        @Schema(description = "商家名称") String merchantName,
        @Schema(description = "头像地址") String avatarUrl,
        @Schema(description = "角色，第一版固定为 MERCHANT") String role) { }
