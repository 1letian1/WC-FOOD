package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "商家商品分页条件")
public record ProductQueryDTO(
        @Schema(description = "页码，默认1") @Min(value = 1, message = "页码不能小于1") Long current,
        @Schema(description = "每页数量，默认10，最大100") @Min(value = 1, message = "每页数量不能小于1") @Max(value = 100, message = "每页数量不能超过100") Long size,
        @Schema(description = "分类 ID") @Positive(message = "分类ID必须为正数") Long categoryId,
        @Schema(description = "商品状态：0下架、1销售中、2售罄") @Min(value = 0, message = "商品状态只能为0、1或2") @Max(value = 2, message = "商品状态只能为0、1或2") Integer status,
        @Schema(description = "商品名称或简介关键词") @Size(max = 100, message = "搜索关键词长度不能超过100位") String keyword) {
    public ProductQueryDTO {
        if (current == null) current = 1L;
        if (size == null) size = 10L;
        if (keyword != null) keyword = keyword.trim();
    }
}
