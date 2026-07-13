package com.shike.ordering.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "加入购物车请求")
public record CartItemAddDTO(
        @Schema(description = "商品 ID") @NotNull(message = "商品ID不能为空") @Positive(message = "商品ID必须为正数") Long productId,
        @Schema(description = "规格 ID；商品无规格时不传") @Positive(message = "规格ID必须为正数") Long specificationId,
        @Schema(description = "口味 ID；不选择口味时不传") @Positive(message = "口味ID必须为正数") Long tasteId,
        @Schema(description = "本次增加数量，1至99") @NotNull(message = "商品数量不能为空")
        @Min(value = 1, message = "商品数量不能小于1") @Max(value = 99, message = "商品数量不能超过99") Integer quantity) { }
