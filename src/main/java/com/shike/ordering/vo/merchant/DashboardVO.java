package com.shike.ordering.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "商家工作台订单统计")
public record DashboardVO(
        @Schema(description = "今日有效订单数，不含已取消和已拒单") Long todayOrderCount,
        @Schema(description = "今日已完成订单营业额") BigDecimal todayTurnover,
        Long pendingAcceptCount, Long cookingCount, Long readyForPickupCount, Long deliveringCount) { }
