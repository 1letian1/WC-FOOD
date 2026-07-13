package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@TableName("product_specification")
public class ProductSpecification extends BaseEntity {
    private Long productId;
    private String name;
    private BigDecimal priceDelta;
    private Integer status;
    private Integer sort;
    @TableLogic
    private Integer deleted;
}
