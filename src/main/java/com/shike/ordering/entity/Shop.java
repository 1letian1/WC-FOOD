package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@TableName("shop")
public class Shop extends BaseEntity {
    private String name;
    private String logoUrl;
    private String phone;
    private String address;
    private String notice;
    private String businessHours;
    private Integer businessStatus;
    private Boolean dineInEnabled;
    private Boolean deliveryEnabled;
    private BigDecimal deliveryFee;
    private BigDecimal minDeliveryAmount;
    private BigDecimal packageFee;
    private String deliveryRange;
    private Integer estimatedDeliveryMinutes;
    @Version
    private Integer version;
}
