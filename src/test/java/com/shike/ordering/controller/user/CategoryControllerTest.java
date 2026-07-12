package com.shike.ordering.controller.user;

import com.shike.ordering.common.filter.RequestIdFilter;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.service.user.CategoryQueryService;
import com.shike.ordering.vo.common.StatusVO;
import com.shike.ordering.vo.user.CategoryVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest {
    @Test
    void listCategories_shouldReturnUnifiedPublicCategoryList() throws Exception {
        CategoryQueryService service = mock(CategoryQueryService.class);
        when(service.listEnabledCategories(any())).thenReturn(List.of(
                new CategoryVO(1L, "热门推荐", new StatusVO(1, "开放"), 10)));
        CategoryController controller = new CategoryController(service, new ShopProperties(1L));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(new RequestIdFilter()).build();

        mockMvc.perform(get("/api/user/categories"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].name").value("热门推荐"))
                .andExpect(jsonPath("$.data[0].status.code").value(1))
                .andExpect(jsonPath("$.data[0].shopId").doesNotExist())
                .andExpect(jsonPath("$.data[0].deleted").doesNotExist());
    }
}
