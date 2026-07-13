package com.shike.ordering.vo.user;

import com.shike.ordering.vo.common.StatusVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "订单创建结果")
public record OrderCreateVO(
        @Schema(description = "订单 ID") Long id,
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "订单类型：1堂食、2配送") StatusVO orderType,
        @Schema(description = "订单状态") StatusVO status,
        @Schema(description = "商品总额") BigDecimal totalAmount,
        @Schema(description = "配送费") BigDecimal deliveryFee,
        @Schema(description = "包装费") BigDecimal packageFee,
        @Schema(description = "实付金额") BigDecimal payAmount,
        @Schema(description = "创建时间") LocalDateTime createTime) { }
