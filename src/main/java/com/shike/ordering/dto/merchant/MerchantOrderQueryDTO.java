package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "商家订单分页条件")
public record MerchantOrderQueryDTO(
        @Schema(description = "页码，默认1") @Min(value = 1, message = "页码不能小于1") Long current,
        @Schema(description = "每页数量，默认10，最大100")
        @Min(value = 1, message = "每页数量不能小于1") @Max(value = 100, message = "每页数量不能超过100") Long size,
        @Schema(description = "订单类型：1堂食、2配送")
        @Min(value = 1, message = "订单类型只能为1或2") @Max(value = 2, message = "订单类型只能为1或2") Integer orderType,
        @Schema(description = "订单状态：1至9")
        @Min(value = 1, message = "订单状态只能为1至9") @Max(value = 9, message = "订单状态只能为1至9") Integer status,
        @Schema(description = "订单号、联系电话或用户昵称") @Size(max = 100, message = "搜索关键词长度不能超过100位") String keyword) {
    public MerchantOrderQueryDTO {
        if (current == null) current = 1L;
        if (size == null) size = 10L;
        if (keyword != null) keyword = keyword.trim();
    }
}
