package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "商品规格或口味")
public record ProductOptionVO(@Schema(description = "选项 ID") Long id,
                              @Schema(description = "选项名称") String name,
                              @Schema(description = "规格加价；口味固定为0") BigDecimal priceDelta,
                              @Schema(description = "启用状态") StatusVO status,
                              @Schema(description = "排序值") Integer sort) { }
