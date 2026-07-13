package com.shike.ordering.controller.user;

import com.shike.ordering.common.exception.GlobalExceptionHandler;
import com.shike.ordering.common.filter.RequestIdFilter;
import com.shike.ordering.service.user.AddressService;
import com.shike.ordering.service.user.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class M5CartAddressControllerTest {
    @Test
    void addCartItem_whenQuantityExceeds99_shouldReturnUnifiedParameterError() throws Exception {
        MockMvc mockMvc = mockMvc(new CartController(mock(CartService.class)));

        mockMvc.perform(post("/api/user/cart/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"quantity\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value("商品数量不能超过99"));
    }

    @Test
    void createAddress_whenPhoneIsInvalid_shouldReturnUnifiedParameterError() throws Exception {
        MockMvc mockMvc = mockMvc(new AddressController(mock(AddressService.class)));

        mockMvc.perform(post("/api/user/addresses").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contactName\":\"小李\",\"phone\":\"123\",\"area\":\"高新区\","
                                + "\"detail\":\"阳光花园\",\"isDefault\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value("联系电话格式不正确"));
    }

    private MockMvc mockMvc(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new RequestIdFilter())
                .build();
    }
}
