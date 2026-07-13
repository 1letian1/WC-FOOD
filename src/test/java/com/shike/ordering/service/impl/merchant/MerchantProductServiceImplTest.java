package com.shike.ordering.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.converter.ProductViewAssembler;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.dto.merchant.ProductSaveDTO;
import com.shike.ordering.entity.Category;
import com.shike.ordering.entity.Product;
import com.shike.ordering.mapper.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerchantProductServiceImplTest {
    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final CategoryMapper categoryMapper = mock(CategoryMapper.class);
    private final ProductSpecificationMapper specificationMapper = mock(ProductSpecificationMapper.class);
    private final ProductTasteMapper tasteMapper = mock(ProductTasteMapper.class);
    private final ProductViewAssembler assembler = mock(ProductViewAssembler.class);
    private final MerchantProductServiceImpl service = new MerchantProductServiceImpl(
            productMapper, categoryMapper, specificationMapper, tasteMapper, assembler);

    @BeforeEach void setPrincipal() {
        PrincipalContext.set(new CurrentPrincipal(2L, PrincipalType.MERCHANT, 7L, "token"));
    }
    @AfterEach void clearPrincipal() { PrincipalContext.clear(); }

    @Test
    void create_shouldAssignShopFromSessionAndNeverFromRequest() {
        Category category = new Category();
        category.setId(3L);
        category.setShopId(7L);
        when(categoryMapper.selectOne(any(Wrapper.class))).thenReturn(category);
        when(specificationMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(tasteMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        doAnswer(invocation -> { ((Product) invocation.getArgument(0)).setId(20L); return 1; })
                .when(productMapper).insert(org.mockito.ArgumentMatchers.<Product>any());

        service.create(request());

        verify(productMapper).insert((Product) argThat((Product product) -> product.getShopId().equals(7L)
                && product.getCategoryId().equals(3L)));
    }

    @Test
    void detail_whenProductIsNotInSessionShop_shouldReturnNotFound() {
        when(productMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.detail(99L)).isInstanceOf(ResourceNotFoundException.class)
                .extracting("code").isEqualTo(40001);
    }

    private ProductSaveDTO request() {
        return new ProductSaveDTO(3L, "牛肉饭", "/files/a.png", "简介", "详情",
                new BigDecimal("28.00"), new BigDecimal("30.00"), 10, 1, true,
                List.of(), List.of());
    }
}
