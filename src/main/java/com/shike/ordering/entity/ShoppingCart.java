package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("shopping_cart")
public class ShoppingCart extends BaseEntity {
    private Long userId;
    private Long shopId;
    private Long productId;
    private Long specificationId;
    private Long tasteId;
    private Integer quantity;
}
