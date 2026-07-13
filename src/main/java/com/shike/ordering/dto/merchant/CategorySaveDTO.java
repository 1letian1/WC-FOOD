package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "分类保存请求")
public record CategorySaveDTO(
        @Schema(description = "分类名称") @NotBlank(message = "分类名称不能为空") @Size(max = 64, message = "分类名称长度不能超过64位") String name,
        @NotNull(message = "分类状态不能为空") @Min(value = 0, message = "分类状态只能为0或1")
        @Max(value = 1, message = "分类状态只能为0或1") @Schema(description = "0 停用，1 启用") Integer status,
        @Schema(description = "排序值，越小越靠前") @NotNull(message = "分类排序不能为空") @Min(value = 0, message = "分类排序不能小于0") Integer sort) { }
