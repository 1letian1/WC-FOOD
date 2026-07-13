package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "订单列表项")
public record OrderSummaryVO(Long id, String orderNo, StatusVO orderType, StatusVO status,
        String userNickname, String contactName, String tableNo, String addressSummary,
        String productSummary, String productImageUrl, Integer totalQuantity,
        BigDecimal payAmount, LocalDateTime createTime) { }
