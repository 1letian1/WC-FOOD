package com.shike.ordering.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "购物车及金额预览")
public record CartVO(
        @Schema(description = "购物车条目") List<CartItemVO> items,
        @Schema(description = "商品总数量") Integer totalQuantity,
        @Schema(description = "当前商品总金额，不含配送费和包装费") BigDecimal totalAmount) { }
