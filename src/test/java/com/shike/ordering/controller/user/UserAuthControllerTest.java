package com.shike.ordering.controller.user;

import com.shike.ordering.auth.service.AuthenticationService;
import com.shike.ordering.common.exception.GlobalExceptionHandler;
import com.shike.ordering.common.filter.RequestIdFilter;
import com.shike.ordering.dto.user.UserProfileUpdateDTO;
import com.shike.ordering.vo.user.UserProfileVO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAuthControllerTest {
    @Test
    void updateProfile_whenRequestIsValid_shouldReturnUpdatedProfile() throws Exception {
        AuthenticationService service = mock(AuthenticationService.class);
        when(service.updateCurrentUser(any())).thenReturn(
                new UserProfileVO(11L, "新昵称", "https://example.com/avatar.png", "13900000000"));
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(put("/api/user/profile").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\" 新昵称 \"}"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"));
        verify(service).updateCurrentUser(any(UserProfileUpdateDTO.class));
    }

    @Test
    void updateProfile_whenNoFieldIsProvided_shouldReturnParameterError() throws Exception {
        AuthenticationService service = mock(AuthenticationService.class);
        MockMvc mockMvc = mockMvc(service);

        mockMvc.perform(put("/api/user/profile").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.message").value("至少提供一项用户资料"));
    }

    private MockMvc mockMvc(AuthenticationService service) {
        return MockMvcBuilders.standaloneSetup(new UserAuthController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new RequestIdFilter())
                .build();
    }
}
