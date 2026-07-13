package com.shike.ordering.controller.user;

import com.shike.ordering.common.exception.GlobalExceptionHandler;
import com.shike.ordering.common.filter.RequestIdFilter;
import com.shike.ordering.service.user.OrderCreationService;
import com.shike.ordering.service.user.UserOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                    new OrderController(mock(OrderCreationService.class), mock(UserOrderService.class)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new RequestIdFilter())
            .build();

    @Test
    void create_whenIdempotencyHeaderIsMissing_shouldReturnUnifiedBadRequest() throws Exception {
        mockMvc.perform(post("/api/user/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(dineInJson()))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.code").value(10002));
    }

    @Test
    void create_whenDeliveryContainsDineInContact_shouldRejectMixedFulfillmentData() throws Exception {
        mockMvc.perform(post("/api/user/orders").header("Idempotency-Key", "idem-key-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderType\":2,\"cartItemIds\":[5],\"contactName\":\"伪造联系人\","
                                + "\"noSeatYet\":false,\"addressId\":7}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value("订单履约信息不完整或与订单类型不匹配"));
    }

    @Test
    void create_whenCartItemIdIsNull_shouldReturnUnifiedBadRequest() throws Exception {
        mockMvc.perform(post("/api/user/orders").header("Idempotency-Key", "idem-key-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderType\":1,\"cartItemIds\":[null],\"contactName\":\"小李\","
                                + "\"contactPhone\":\"13800000000\",\"tableNo\":\"A06\",\"noSeatYet\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value("购物车条目ID不能为空"));
    }

    private String dineInJson() {
        return "{\"orderType\":1,\"cartItemIds\":[5],\"contactName\":\"小李\","
                + "\"contactPhone\":\"13800000000\",\"tableNo\":\"A06\",\"noSeatYet\":false}";
    }
}
