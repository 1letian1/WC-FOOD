package com.shike.ordering.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.common.result.PageResult;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.converter.ProductViewAssembler;
import com.shike.ordering.dto.user.ProductQueryDTO;
import com.shike.ordering.entity.Category;
import com.shike.ordering.entity.Product;
import com.shike.ordering.mapper.CategoryMapper;
import com.shike.ordering.mapper.ProductMapper;
import com.shike.ordering.service.user.ProductQueryService;
import com.shike.ordering.vo.common.ProductDetailVO;
import com.shike.ordering.vo.common.ProductSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final ProductViewAssembler assembler;
    private final ShopProperties shopProperties;

    @Override
    public PageResult<ProductSummaryVO> list(ProductQueryDTO query) {
        Page<Product> page = new Page<>(query.current(), query.size());
        var result = productMapper.selectUserPage(page, shopProperties.defaultShopId(), query.categoryId(), query.keyword());
        return new PageResult<>(assembler.summaries(result.getRecords()), result.getTotal(),
                result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public ProductDetailVO detail(Long id) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id).eq(Product::getShopId, shopProperties.defaultShopId()));
        if (product == null) throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        if (product.getStatus() == 0) throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE);
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getId, product.getCategoryId()).eq(Category::getShopId, product.getShopId())
                .eq(Category::getStatus, 1));
        if (category == null) throw new BusinessException(ErrorCode.PRODUCT_OFF_SALE);
        return assembler.detail(product, true);
    }
}
