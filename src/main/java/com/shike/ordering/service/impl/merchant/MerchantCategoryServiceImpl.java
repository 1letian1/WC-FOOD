package com.shike.ordering.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.converter.CategoryConverter;
import com.shike.ordering.dto.merchant.CategorySaveDTO;
import com.shike.ordering.entity.Category;
import com.shike.ordering.entity.Product;
import com.shike.ordering.mapper.CategoryMapper;
import com.shike.ordering.mapper.ProductMapper;
import com.shike.ordering.service.merchant.MerchantCategoryService;
import com.shike.ordering.vo.merchant.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantCategoryServiceImpl implements MerchantCategoryService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final CategoryConverter converter;

    @Override
    public List<CategoryVO> list() {
        Long shopId = shopId();
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().eq(Category::getShopId, shopId)
                        .orderByAsc(Category::getSort).orderByAsc(Category::getId))
                .stream().map(converter::toMerchantVO).toList();
    }

    @Override
    public CategoryVO create(CategorySaveDTO request) {
        Category category = new Category();
        category.setShopId(shopId());
        apply(category, request);
        category.setDeleted(0);
        categoryMapper.insert(category);
        log.info("merchant created category, shopId={}, categoryId={}", category.getShopId(), category.getId());
        return converter.toMerchantVO(category);
    }

    @Override
    public CategoryVO update(Long id, CategorySaveDTO request) {
        Category category = requireOwned(id);
        apply(category, request);
        categoryMapper.updateById(category);
        return converter.toMerchantVO(category);
    }

    @Override
    public CategoryVO updateStatus(Long id, Integer status) {
        Category category = requireOwned(id);
        category.setStatus(status);
        categoryMapper.updateById(category);
        return converter.toMerchantVO(category);
    }

    @Override
    public void delete(Long id) {
        Category category = requireOwned(id);
        long products = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, category.getShopId()).eq(Product::getCategoryId, id));
        if (products > 0) throw new BusinessException(ErrorCode.DATA_CONFLICT);
        categoryMapper.deleteById(category);
        log.info("merchant deleted category, shopId={}, categoryId={}", category.getShopId(), id);
    }

    private Category requireOwned(Long id) {
        Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                .eq(Category::getId, id).eq(Category::getShopId, shopId()));
        if (category == null) throw new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND);
        return category;
    }

    private Long shopId() {
        return PrincipalContext.require(PrincipalType.MERCHANT).shopId();
    }

    private void apply(Category category, CategorySaveDTO request) {
        category.setName(request.name().trim());
        category.setStatus(request.status());
        category.setSort(request.sort());
    }
}
