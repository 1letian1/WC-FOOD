package com.shike.ordering.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "购物车数量修改请求")
public record CartItemQuantityDTO(
        @Schema(description = "修改后的数量，1至99") @NotNull(message = "商品数量不能为空")
        @Min(value = 1, message = "商品数量不能小于1") @Max(value = 99, message = "商品数量不能超过99") Integer quantity) { }
