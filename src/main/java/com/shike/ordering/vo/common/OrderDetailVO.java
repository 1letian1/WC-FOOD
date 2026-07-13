package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "订单详情")
public record OrderDetailVO(Long id, String orderNo, StatusVO orderType, StatusVO status, String userNickname,
        String contactName, String contactPhone, String tableNo, Boolean noSeatYet,
        Long addressId, String addressArea, String addressDetail, String addressHouseNumber,
        String deliveryRangeSnapshot, String remark, String rejectReason,
        BigDecimal totalAmount, BigDecimal deliveryFee, BigDecimal packageFee, BigDecimal payAmount,
        LocalDateTime createTime, LocalDateTime acceptTime, LocalDateTime cookingTime,
        LocalDateTime readyTime, LocalDateTime deliveryTime, LocalDateTime deliveredTime,
        LocalDateTime cancelTime, LocalDateTime finishTime, List<Item> items, List<StatusLog> statusLogs) {
    public OrderDetailVO {
        items = items == null ? List.of() : List.copyOf(items);
        statusLogs = statusLogs == null ? List.of() : List.copyOf(statusLogs);
    }
    @Schema(description = "订单商品快照")
    public record Item(Long id, Long productId, String productName, String productImageUrl,
            String specificationName, String tasteName, BigDecimal unitPrice, Integer quantity, BigDecimal amount) { }
    @Schema(description = "订单状态日志")
    public record StatusLog(StatusVO fromStatus, StatusVO toStatus, String operatorType,
            String operatorDescription, String reason, Integer orderVersion, LocalDateTime createTime) { }
}
