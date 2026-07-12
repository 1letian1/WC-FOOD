package com.shike.ordering.service.impl.user;

import com.shike.ordering.converter.CategoryConverter;
import com.shike.ordering.dto.user.CategoryQueryDTO;
import com.shike.ordering.entity.Category;
import com.shike.ordering.mapper.CategoryMapper;
import com.shike.ordering.vo.user.CategoryVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryQueryServiceImplTest {
    @Test
    void listEnabledCategories_shouldMapSingleQueryResultWithoutAdditionalQueries() {
        CategoryMapper mapper = mock(CategoryMapper.class);
        CategoryQueryServiceImpl service = new CategoryQueryServiceImpl(mapper, new CategoryConverter());
        when(mapper.selectEnabledByShopId(1L)).thenReturn(List.of(category(1L, "热门推荐", 10),
                category(2L, "主食", 20)));

        List<CategoryVO> result = service.listEnabledCategories(new CategoryQueryDTO(1L));

        assertThat(result).extracting(CategoryVO::name).containsExactly("热门推荐", "主食");
        assertThat(result).allSatisfy(category -> {
            assertThat(category.status().code()).isEqualTo(1);
            assertThat(category.status().description()).isEqualTo("开放");
        });
        verify(mapper).selectEnabledByShopId(1L);
    }

    @Test
    void listEnabledCategories_whenNoCategory_shouldReturnEmptyList() {
        CategoryMapper mapper = mock(CategoryMapper.class);
        CategoryQueryServiceImpl service = new CategoryQueryServiceImpl(mapper, new CategoryConverter());
        when(mapper.selectEnabledByShopId(1L)).thenReturn(List.of());

        assertThat(service.listEnabledCategories(new CategoryQueryDTO(1L))).isEmpty();
    }

    private Category category(Long id, String name, int sort) {
        Category category = new Category();
        category.setId(id);
        category.setShopId(1L);
        category.setName(name);
        category.setStatus(1);
        category.setSort(sort);
        category.setDeleted(0);
        return category;
    }
}
