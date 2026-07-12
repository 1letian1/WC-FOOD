package com.shike.ordering.controller.common;

import com.shike.ordering.common.filter.RequestIdFilter;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.GlobalExceptionHandler;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.config.ShopProperties;
import com.shike.ordering.service.common.ShopQueryService;
import com.shike.ordering.vo.common.ShopPublicVO;
import com.shike.ordering.vo.common.StatusVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShopControllerTest {
    @Test
    void getPublicShop_shouldReturnUnifiedPublicResponse() throws Exception {
        ShopQueryService service = mock(ShopQueryService.class);
        when(service.getPublicShop(any())).thenReturn(shopView());
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(get("/api/common/shop"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("食刻小馆"))
                .andExpect(jsonPath("$.data.businessStatus.code").value(1))
                .andExpect(jsonPath("$.data.deliveryFee").value(3.00))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.openid").doesNotExist());
    }

    @Test
    void getPublicShop_whenDefaultShopMissing_shouldReturnUnifiedNotFoundResponse() throws Exception {
        ShopQueryService service = mock(ShopQueryService.class);
        when(service.getPublicShop(any())).thenThrow(new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        mockMvc(service).perform(get("/api/common/shop"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(70005))
                .andExpect(jsonPath("$.message").value("店铺不存在"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private MockMvc mockMvc(ShopQueryService service) {
        ShopController controller = new ShopController(service, new ShopProperties(1L));
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new RequestIdFilter())
                .build();
    }

    private ShopPublicVO shopView() {
        StatusVO enabled = new StatusVO(1, "开放");
        return new ShopPublicVO(1L, "食刻小馆", null, "13800000000", "测试地址", "欢迎光临",
                "09:00-22:00", new StatusVO(1, "营业中"), enabled, enabled, new BigDecimal("3.00"),
                new BigDecimal("20.00"), BigDecimal.ZERO, "商家配送", 30);
    }
}
