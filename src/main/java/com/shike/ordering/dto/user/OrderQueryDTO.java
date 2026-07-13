package com.shike.ordering.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "用户订单分页条件")
public record OrderQueryDTO(
        @Schema(description = "页码，默认1") @Min(value = 1, message = "页码不能小于1") Long current,
        @Schema(description = "每页数量，默认10，最大100")
        @Min(value = 1, message = "每页数量不能小于1") @Max(value = 100, message = "每页数量不能超过100") Long size,
        @Schema(description = "订单类型：1堂食、2配送")
        @Min(value = 1, message = "订单类型只能为1或2") @Max(value = 2, message = "订单类型只能为1或2") Integer orderType,
        @Schema(description = "订单状态：1待接单、2已接单、3制作中、4待取餐、5配送中、6已送达、7已完成、8已取消、9已拒单")
        @Min(value = 1, message = "订单状态只能为1至9") @Max(value = 9, message = "订单状态只能为1至9") Integer status) {
    public OrderQueryDTO {
        if (current == null) current = 1L;
        if (size == null) size = 10L;
    }
}
