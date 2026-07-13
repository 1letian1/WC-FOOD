package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "商品详情")
public record ProductDetailVO(@Schema(description = "商品 ID") Long id,
                              @Schema(description = "分类 ID") Long categoryId,
                              @Schema(description = "分类名称") String categoryName,
                              @Schema(description = "商品名称") String name,
                              @Schema(description = "商品图片地址") String imageUrl,
                              @Schema(description = "商品简介") String description,
                              @Schema(description = "商品详情") String detail,
                              @Schema(description = "当前价格") BigDecimal price,
                              @Schema(description = "原价") BigDecimal originalPrice,
                              @Schema(description = "库存") Integer stock,
                              @Schema(description = "商品状态") StatusVO status,
                              @Schema(description = "是否推荐") Boolean recommended,
                              @Schema(description = "规格列表") List<ProductOptionVO> specifications,
                              @Schema(description = "口味列表") List<ProductOptionVO> tastes,
                              @Schema(description = "并发版本号") Integer version) {
    public ProductDetailVO {
        specifications = specifications == null ? List.of() : List.copyOf(specifications);
        tastes = tastes == null ? List.of() : List.copyOf(tastes);
    }
}
