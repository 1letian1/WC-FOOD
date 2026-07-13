package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "商品保存请求")
public record ProductSaveDTO(
        @Schema(description = "所属分类 ID") @NotNull(message = "商品分类不能为空") @Positive(message = "商品分类ID必须为正数") Long categoryId,
        @Schema(description = "商品名称") @NotBlank(message = "商品名称不能为空") @Size(max = 100, message = "商品名称长度不能超过100位") String name,
        @Schema(description = "商品图片地址") @NotBlank(message = "商品图片不能为空") @Size(max = 500, message = "商品图片地址长度不能超过500位") String imageUrl,
        @Schema(description = "商品简介") @Size(max = 255, message = "商品简介长度不能超过255位") String description,
        @Schema(description = "商品详情") @Size(max = 5000, message = "商品详情长度不能超过5000位") String detail,
        @Schema(description = "商品基础价格") @NotNull(message = "商品价格不能为空") @DecimalMin(value = "0.01", message = "商品价格必须大于0")
        @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Schema(description = "商品原价，可空且不得低于当前价格") @DecimalMin(value = "0.00", message = "商品原价不能小于0") @Digits(integer = 8, fraction = 2) BigDecimal originalPrice,
        @Schema(description = "商品级库存") @NotNull(message = "商品库存不能为空") @Min(value = 0, message = "商品库存不能小于0") Integer stock,
        @Schema(description = "商品状态：0下架、1销售中、2售罄") @NotNull(message = "商品状态不能为空") @Min(value = 0, message = "商品状态只能为0、1或2")
        @Max(value = 2, message = "商品状态只能为0、1或2") Integer status,
        @Schema(description = "是否推荐") @NotNull(message = "推荐状态不能为空") Boolean recommended,
        @Schema(description = "规格完整列表") @Valid @Size(max = 20, message = "商品规格不能超过20个") List<SpecificationInput> specifications,
        @Schema(description = "口味完整列表") @Valid @Size(max = 20, message = "商品口味不能超过20个") List<TasteInput> tastes) {
    public ProductSaveDTO {
        specifications = specifications == null ? List.of() : List.copyOf(specifications);
        tastes = tastes == null ? List.of() : List.copyOf(tastes);
    }

    public record SpecificationInput(
            @Schema(description = "已有规格 ID；新增时不传") @Positive(message = "规格ID必须为正数") Long id,
            @Schema(description = "规格名称") @NotBlank(message = "规格名称不能为空") @Size(max = 64, message = "规格名称长度不能超过64位") String name,
            @Schema(description = "规格加价") @NotNull(message = "规格加价不能为空") @DecimalMin(value = "0.00", message = "规格加价不能小于0")
            @Digits(integer = 8, fraction = 2) BigDecimal priceDelta,
            @Schema(description = "0停用，1启用") @NotNull(message = "规格状态不能为空") @Min(value = 0) @Max(value = 1) Integer status,
            @Schema(description = "排序值") @NotNull(message = "规格排序不能为空") @Min(value = 0) Integer sort) { }

    public record TasteInput(
            @Schema(description = "已有口味 ID；新增时不传") @Positive(message = "口味ID必须为正数") Long id,
            @Schema(description = "口味名称") @NotBlank(message = "口味名称不能为空") @Size(max = 64, message = "口味名称长度不能超过64位") String name,
            @Schema(description = "0停用，1启用") @NotNull(message = "口味状态不能为空") @Min(value = 0) @Max(value = 1) Integer status,
            @Schema(description = "排序值") @NotNull(message = "口味排序不能为空") @Min(value = 0) Integer sort) { }
}
