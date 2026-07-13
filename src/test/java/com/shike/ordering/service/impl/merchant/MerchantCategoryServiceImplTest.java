package com.shike.ordering.service.impl.merchant;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.converter.CategoryConverter;
import com.shike.ordering.dto.merchant.CategorySaveDTO;
import com.shike.ordering.entity.Category;
import com.shike.ordering.mapper.CategoryMapper;
import com.shike.ordering.mapper.ProductMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MerchantCategoryServiceImplTest {
    private final CategoryMapper categoryMapper = mock(CategoryMapper.class);
    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final MerchantCategoryServiceImpl service =
            new MerchantCategoryServiceImpl(categoryMapper, productMapper, new CategoryConverter());

    @BeforeEach void setPrincipal() {
        PrincipalContext.set(new CurrentPrincipal(2L, PrincipalType.MERCHANT, 7L, "token"));
    }
    @AfterEach void clearPrincipal() { PrincipalContext.clear(); }

    @Test
    void create_shouldAlwaysUseShopIdFromSession() {
        doAnswer(invocation -> { ((Category) invocation.getArgument(0)).setId(9L); return 1; })
                .when(categoryMapper).insert(org.mockito.ArgumentMatchers.<Category>any());

        var result = service.create(new CategorySaveDTO("主食", 1, 0));

        assertThat(result.id()).isEqualTo(9L);
        verify(categoryMapper).insert((Category) argThat(
                (Category category) -> category.getShopId().equals(7L)));
    }

    @Test
    void delete_whenCategoryHasProducts_shouldReject() {
        Category category = new Category();
        category.setId(9L);
        category.setShopId(7L);
        when(categoryMapper.selectOne(any(Wrapper.class))).thenReturn(category);
        when(productMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThatThrownBy(() -> service.delete(9L)).isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(10006);
        verify(categoryMapper, never()).deleteById(any(Category.class));
    }
}
