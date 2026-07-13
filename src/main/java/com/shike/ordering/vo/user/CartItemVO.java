package com.shike.ordering.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "购物车条目")
public class CartItemVO {
    @Schema(description = "购物车条目 ID") private Long id;
    @Schema(description = "商品 ID") private Long productId;
    @Schema(description = "商品名称") private String productName;
    @Schema(description = "商品图片地址") private String productImageUrl;
    @Schema(description = "规格 ID") private Long specificationId;
    @Schema(description = "规格名称") private String specificationName;
    @Schema(description = "口味 ID") private Long tasteId;
    @Schema(description = "口味名称") private String tasteName;
    @Schema(description = "当前单价，含规格加价") private BigDecimal unitPrice;
    @Schema(description = "数量") private Integer quantity;
    @Schema(description = "当前小计金额") private BigDecimal amount;
    @Schema(description = "当前是否可结算") private Boolean available;
}
