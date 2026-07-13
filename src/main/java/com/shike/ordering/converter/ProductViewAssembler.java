package com.shike.ordering.converter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shike.ordering.common.enums.EnabledStatus;
import com.shike.ordering.common.enums.ProductStatus;
import com.shike.ordering.entity.*;
import com.shike.ordering.mapper.*;
import com.shike.ordering.vo.common.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductViewAssembler {
    private final CategoryMapper categoryMapper;
    private final ProductSpecificationMapper specificationMapper;
    private final ProductTasteMapper tasteMapper;

    public List<ProductSummaryVO> summaries(List<Product> products) {
        if (products.isEmpty()) return List.of();
        Map<Long, String> categoryNames = categoryMapper.selectBatchIds(
                        products.stream().map(Product::getCategoryId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Category::getId, Category::getName));
        Set<Long> productIdsWithSpecs = specificationMapper.selectList(new LambdaQueryWrapper<ProductSpecification>()
                        .in(ProductSpecification::getProductId, products.stream().map(Product::getId).toList())
                        .eq(ProductSpecification::getStatus, 1))
                .stream().map(ProductSpecification::getProductId).collect(Collectors.toSet());
        return products.stream().map(product -> new ProductSummaryVO(product.getId(), product.getCategoryId(),
                categoryNames.get(product.getCategoryId()), product.getName(), product.getImageUrl(),
                product.getDescription(), product.getPrice(), product.getOriginalPrice(), product.getStock(),
                productStatus(product.getStatus()), product.getRecommended(), productIdsWithSpecs.contains(product.getId())))
                .toList();
    }

    public ProductDetailVO detail(Product product, boolean enabledOptionsOnly) {
        Category category = categoryMapper.selectById(product.getCategoryId());
        LambdaQueryWrapper<ProductSpecification> specificationQuery = new LambdaQueryWrapper<ProductSpecification>()
                .eq(ProductSpecification::getProductId, product.getId())
                .eq(enabledOptionsOnly, ProductSpecification::getStatus, 1)
                .orderByAsc(ProductSpecification::getSort).orderByAsc(ProductSpecification::getId);
        LambdaQueryWrapper<ProductTaste> tasteQuery = new LambdaQueryWrapper<ProductTaste>()
                .eq(ProductTaste::getProductId, product.getId())
                .eq(enabledOptionsOnly, ProductTaste::getStatus, 1)
                .orderByAsc(ProductTaste::getSort).orderByAsc(ProductTaste::getId);
        List<ProductOptionVO> specifications = specificationMapper.selectList(specificationQuery).stream()
                .map(option -> option(option.getId(), option.getName(), option.getPriceDelta(), option.getStatus(), option.getSort()))
                .toList();
        List<ProductOptionVO> tastes = tasteMapper.selectList(tasteQuery).stream()
                .map(option -> option(option.getId(), option.getName(), BigDecimal.ZERO, option.getStatus(), option.getSort()))
                .toList();
        return new ProductDetailVO(product.getId(), product.getCategoryId(), category == null ? null : category.getName(),
                product.getName(), product.getImageUrl(), product.getDescription(), product.getDetail(),
                product.getPrice(), product.getOriginalPrice(), product.getStock(), productStatus(product.getStatus()),
                product.getRecommended(), specifications, tastes, product.getVersion());
    }

    private StatusVO productStatus(Integer code) {
        ProductStatus status = ProductStatus.fromCode(code);
        return new StatusVO(status.getCode(), status.getDescription());
    }

    private ProductOptionVO option(Long id, String name, BigDecimal priceDelta, Integer code, Integer sort) {
        EnabledStatus status = code == 1 ? EnabledStatus.ENABLED : EnabledStatus.DISABLED;
        return new ProductOptionVO(id, name, priceDelta, new StatusVO(status.getCode(), status.getDescription()), sort);
    }
}
