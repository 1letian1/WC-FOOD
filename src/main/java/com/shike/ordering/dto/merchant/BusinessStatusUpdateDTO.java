package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "店铺营业状态请求")
public record BusinessStatusUpdateDTO(
        @NotNull(message = "营业状态不能为空") @Min(value = 0, message = "营业状态只能为0或1")
        @Max(value = 1, message = "营业状态只能为0或1")
        @Schema(description = "0 休息，1 营业") Integer status) { }
