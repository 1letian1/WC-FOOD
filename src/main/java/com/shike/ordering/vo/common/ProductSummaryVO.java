package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "商品摘要")
public record ProductSummaryVO(@Schema(description = "商品 ID") Long id,
                               @Schema(description = "分类 ID") Long categoryId,
                               @Schema(description = "分类名称") String categoryName,
                               @Schema(description = "商品名称") String name,
                               @Schema(description = "商品图片地址") String imageUrl,
                               @Schema(description = "商品简介") String description,
                               @Schema(description = "当前价格") BigDecimal price,
                               @Schema(description = "原价") BigDecimal originalPrice,
                               @Schema(description = "库存") Integer stock,
                               @Schema(description = "商品状态：0下架、1销售中、2售罄") StatusVO status,
                               @Schema(description = "是否推荐") Boolean recommended,
                               @Schema(description = "是否存在启用规格") Boolean hasSpecifications) { }
