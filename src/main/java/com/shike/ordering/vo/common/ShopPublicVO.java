package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "店铺公开信息")
public record ShopPublicVO(
        @Schema(description = "店铺ID", example = "1") Long id,
        @Schema(description = "店铺名称", example = "食刻小馆") String name,
        @Schema(description = "Logo URL") String logoUrl,
        @Schema(description = "联系电话", example = "13800000000") String phone,
        @Schema(description = "店铺地址") String address,
        @Schema(description = "店铺公告") String notice,
        @Schema(description = "营业时间", example = "09:00-22:00") String businessHours,
        @Schema(description = "营业状态") StatusVO businessStatus,
        @Schema(description = "堂食开放状态") StatusVO dineInStatus,
        @Schema(description = "配送开放状态") StatusVO deliveryStatus,
        @Schema(description = "配送费") BigDecimal deliveryFee,
        @Schema(description = "起送金额") BigDecimal minDeliveryAmount,
        @Schema(description = "包装费") BigDecimal packageFee,
        @Schema(description = "配送范围文字说明") String deliveryRange,
        @Schema(description = "预计配送分钟数", example = "30") Integer estimatedDeliveryMinutes) {
}
