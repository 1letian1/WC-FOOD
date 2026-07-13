package com.shike.ordering.mapper.projection;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderDashboardProjection {
    private Long todayOrderCount;
    private BigDecimal todayTurnover;
    private Long pendingAcceptCount;
    private Long cookingCount;
    private Long readyForPickupCount;
    private Long deliveringCount;
}
