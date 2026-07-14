package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "订单列表项")
public record OrderSummaryVO(
        @Schema(description = "订单ID") Long id,
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "订单类型：1堂食、2配送") StatusVO orderType,
        @Schema(description = "订单状态：1待接单至9已拒单") StatusVO status,
        @Schema(description = "用户昵称；用户端查询时可空") String userNickname,
        @Schema(description = "联系人") String contactName,
        @Schema(description = "堂食桌号") String tableNo,
        @Schema(description = "配送地址摘要") String addressSummary,
        @Schema(description = "商品摘要") String productSummary,
        @Schema(description = "首件商品图片") String productImageUrl,
        @Schema(description = "商品总数量") Integer totalQuantity,
        @Schema(description = "订单应付金额") BigDecimal payAmount,
        @Schema(description = "下单时间") LocalDateTime createTime) { }
