package com.shike.ordering.controller.merchant;

import com.shike.ordering.common.exception.GlobalExceptionHandler;
import com.shike.ordering.common.filter.RequestIdFilter;
import com.shike.ordering.service.merchant.MerchantOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MerchantOrderControllerTest {
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                    new MerchantOrderController(mock(MerchantOrderService.class)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new RequestIdFilter())
            .build();

    @Test
    void reject_whenReasonIsBlank_shouldReturnUnifiedBadRequest() throws Exception {
        mockMvc.perform(put("/api/merchant/orders/8/reject")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value("拒单原因不能为空"));
    }
}
