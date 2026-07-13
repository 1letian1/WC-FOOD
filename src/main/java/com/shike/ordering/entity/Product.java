package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@TableName("product")
public class Product extends BaseEntity {
    private Long shopId;
    private Long categoryId;
    private String name;
    private String imageUrl;
    private String description;
    private String detail;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer status;
    private Boolean recommended;
    @TableLogic
    private Integer deleted;
    @Version
    private Integer version;
}
