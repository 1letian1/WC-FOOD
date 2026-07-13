package com.shike.ordering.vo.merchant;

import com.shike.ordering.vo.common.StatusVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "商家店铺设置")
public record ShopVO(@Schema(description = "店铺 ID") Long id,
                     @Schema(description = "店铺名称") String name,
                     @Schema(description = "Logo 地址") String logoUrl,
                     @Schema(description = "联系电话") String phone,
                     @Schema(description = "店铺地址") String address,
                     @Schema(description = "店铺公告") String notice,
                     @Schema(description = "营业时间") String businessHours,
                     @Schema(description = "营业状态") StatusVO businessStatus,
                     @Schema(description = "堂食开放状态") StatusVO dineInStatus,
                     @Schema(description = "配送开放状态") StatusVO deliveryStatus,
                     @Schema(description = "配送费") BigDecimal deliveryFee,
                     @Schema(description = "起送金额") BigDecimal minDeliveryAmount,
                     @Schema(description = "包装费") BigDecimal packageFee,
                     @Schema(description = "配送范围说明") String deliveryRange,
                     @Schema(description = "预计配送分钟数") Integer estimatedDeliveryMinutes,
                     @Schema(description = "并发版本号") Integer version) { }
