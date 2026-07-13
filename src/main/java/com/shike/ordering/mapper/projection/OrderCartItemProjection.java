package com.shike.ordering.mapper.projection;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderCartItemProjection {
    private Long cartId;
    private Long shopId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer productStatus;
    private Integer productStock;
    private Boolean productExists;
    private Boolean categoryAvailable;
    private Long specificationId;
    private String specificationName;
    private Boolean specificationValid;
    private Long tasteId;
    private String tasteName;
    private Boolean tasteValid;
    private BigDecimal unitPrice;
    private Integer quantity;
}
