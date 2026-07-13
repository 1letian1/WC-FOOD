package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "分类状态请求")
public record CategoryStatusDTO(
        @NotNull(message = "分类状态不能为空") @Min(value = 0, message = "分类状态只能为0或1")
        @Max(value = 1, message = "分类状态只能为0或1")
        @Schema(description = "0 停用，1 启用") Integer status) { }
