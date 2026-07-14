package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "订单详情")
public record OrderDetailVO(
        @Schema(description = "订单ID") Long id,
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "订单类型：1堂食、2配送") StatusVO orderType,
        @Schema(description = "订单状态：1待接单至9已拒单") StatusVO status,
        @Schema(description = "用户昵称；用户端查询时可空") String userNickname,
        @Schema(description = "联系人") String contactName,
        @Schema(description = "联系电话") String contactPhone,
        @Schema(description = "堂食桌号") String tableNo,
        @Schema(description = "是否暂未入座") Boolean noSeatYet,
        @Schema(description = "原收货地址ID") Long addressId,
        @Schema(description = "配送区域快照") String addressArea,
        @Schema(description = "详细地址快照") String addressDetail,
        @Schema(description = "门牌号快照") String addressHouseNumber,
        @Schema(description = "配送范围说明快照") String deliveryRangeSnapshot,
        @Schema(description = "用户备注") String remark,
        @Schema(description = "商家拒单原因") String rejectReason,
        @Schema(description = "商品总额") BigDecimal totalAmount,
        @Schema(description = "配送费") BigDecimal deliveryFee,
        @Schema(description = "包装费") BigDecimal packageFee,
        @Schema(description = "订单应付金额") BigDecimal payAmount,
        @Schema(description = "下单时间") LocalDateTime createTime,
        @Schema(description = "接单时间") LocalDateTime acceptTime,
        @Schema(description = "开始制作时间") LocalDateTime cookingTime,
        @Schema(description = "待取餐时间") LocalDateTime readyTime,
        @Schema(description = "开始配送时间") LocalDateTime deliveryTime,
        @Schema(description = "送达时间") LocalDateTime deliveredTime,
        @Schema(description = "取消时间") LocalDateTime cancelTime,
        @Schema(description = "完成时间") LocalDateTime finishTime,
        @Schema(description = "商品快照列表") List<Item> items,
        @Schema(description = "状态日志列表") List<StatusLog> statusLogs) {
    public OrderDetailVO {
        items = items == null ? List.of() : List.copyOf(items);
        statusLogs = statusLogs == null ? List.of() : List.copyOf(statusLogs);
    }
    @Schema(description = "订单商品快照")
    public record Item(
            @Schema(description = "明细ID") Long id,
            @Schema(description = "原商品ID") Long productId,
            @Schema(description = "商品名称快照") String productName,
            @Schema(description = "商品图片快照") String productImageUrl,
            @Schema(description = "规格名称快照") String specificationName,
            @Schema(description = "口味名称快照") String tasteName,
            @Schema(description = "成交单价") BigDecimal unitPrice,
            @Schema(description = "数量") Integer quantity,
            @Schema(description = "明细金额") BigDecimal amount) { }
    @Schema(description = "订单状态日志")
    public record StatusLog(
            @Schema(description = "原状态；初始日志为空") StatusVO fromStatus,
            @Schema(description = "目标状态") StatusVO toStatus,
            @Schema(description = "操作人类型：USER、MERCHANT或SYSTEM") String operatorType,
            @Schema(description = "操作人类型中文说明") String operatorDescription,
            @Schema(description = "操作原因") String reason,
            @Schema(description = "变更后的订单版本") Integer orderVersion,
            @Schema(description = "状态变更时间") LocalDateTime createTime) { }
}
