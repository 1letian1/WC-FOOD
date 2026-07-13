package com.shike.ordering.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.shike.ordering.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("orders")
public class Order extends BaseEntity {
    private String orderNo;
    private String idempotencyKey;
    private Long userId;
    private Long shopId;
    private Integer orderType;
    private Integer status;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal packageFee;
    private BigDecimal payAmount;
    private String contactName;
    private String contactPhone;
    private String tableNo;
    private Boolean noSeatYet;
    private Long addressId;
    private String addressArea;
    private String addressDetail;
    private String addressHouseNumber;
    private String deliveryRangeSnapshot;
    private String remark;
    private String rejectReason;
    @Version
    private Integer version;
    private LocalDateTime acceptTime;
    private LocalDateTime cookingTime;
    private LocalDateTime readyTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime deliveredTime;
    private LocalDateTime cancelTime;
    private LocalDateTime finishTime;
}
