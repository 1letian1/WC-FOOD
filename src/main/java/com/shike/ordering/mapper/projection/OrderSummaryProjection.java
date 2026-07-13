package com.shike.ordering.mapper.projection;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrderSummaryProjection {
    private Long id;
    private String orderNo;
    private Integer orderType;
    private Integer status;
    private String userNickname;
    private String contactName;
    private String tableNo;
    private String addressSummary;
    private String productSummary;
    private String productImageUrl;
    private Integer totalQuantity;
    private BigDecimal payAmount;
    private LocalDateTime createTime;
}
