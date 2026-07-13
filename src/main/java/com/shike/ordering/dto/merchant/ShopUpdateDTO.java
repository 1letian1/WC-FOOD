package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "店铺设置请求")
public record ShopUpdateDTO(
        @Schema(description = "店铺名称") @NotBlank(message = "店铺名称不能为空") @Size(max = 100, message = "店铺名称长度不能超过100位") String name,
        @Schema(description = "店铺 Logo 地址") @Size(max = 500, message = "Logo 地址长度不能超过500位") String logoUrl,
        @Schema(description = "店铺联系电话") @NotBlank(message = "店铺联系电话不能为空") @Size(max = 20, message = "店铺联系电话长度不能超过20位") String phone,
        @Schema(description = "店铺地址") @NotBlank(message = "店铺地址不能为空") @Size(max = 255, message = "店铺地址长度不能超过255位") String address,
        @Schema(description = "店铺公告") @Size(max = 500, message = "店铺公告长度不能超过500位") String notice,
        @Schema(description = "营业时间展示文本") @NotBlank(message = "营业时间不能为空") @Size(max = 255, message = "营业时间长度不能超过255位") String businessHours,
        @Schema(description = "配送费") @NotNull(message = "配送费不能为空") @DecimalMin(value = "0.00", message = "配送费不能小于0") @Digits(integer = 8, fraction = 2) BigDecimal deliveryFee,
        @Schema(description = "起送金额") @NotNull(message = "起送金额不能为空") @DecimalMin(value = "0.00", message = "起送金额不能小于0") @Digits(integer = 8, fraction = 2) BigDecimal minDeliveryAmount,
        @Schema(description = "包装费") @NotNull(message = "包装费不能为空") @DecimalMin(value = "0.00", message = "包装费不能小于0") @Digits(integer = 8, fraction = 2) BigDecimal packageFee,
        @Schema(description = "配送范围文字说明") @Size(max = 500, message = "配送范围说明长度不能超过500位") String deliveryRange,
        @Schema(description = "预计配送分钟数") @Min(value = 1, message = "预计配送时间至少为1分钟") @Max(value = 1440, message = "预计配送时间不能超过1440分钟") Integer estimatedDeliveryMinutes) { }
