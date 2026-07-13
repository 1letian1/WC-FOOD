package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@TableName("order_item")
public class OrderItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long specificationId;
    private String specificationName;
    private Long tasteId;
    private String tasteName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal amount;
}
