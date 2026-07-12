package com.shike.ordering.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shike.ordering.auth.service.RedisSessionService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthenticationFilterTest {
    @Test
    void doFilter_whenRequestingPublicShop_shouldNotReadSession() throws Exception {
        RedisSessionService sessionService = mock(RedisSessionService.class);
        AuthenticationFilter filter = new AuthenticationFilter(sessionService, new ObjectMapper());
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/common/shop");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(sessionService);
    }

    @Test
    void doFilter_whenRequestingPublicCategories_shouldNotReadSession() throws Exception {
        RedisSessionService sessionService = mock(RedisSessionService.class);
        AuthenticationFilter filter = new AuthenticationFilter(sessionService, new ObjectMapper());
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/categories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(sessionService);
    }
}
