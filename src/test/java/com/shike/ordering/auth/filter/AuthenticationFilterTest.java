package com.shike.ordering.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shike.ordering.auth.service.RedisSessionService;
import com.shike.ordering.auth.model.AuthSession;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.Optional;

class AuthenticationFilterTest {
    @Test
    void doFilter_whenRequestingPublicShop_shouldNotReadSession() throws Exception {
        RedisSessionService sessionService = mock(RedisSessionService.class);
        AuthenticationFilter filter = new AuthenticationFilter(sessionService, new ObjectMapper().findAndRegisterModules());
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
        AuthenticationFilter filter = new AuthenticationFilter(sessionService, new ObjectMapper().findAndRegisterModules());
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/categories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(sessionService);
    }

    @Test
    void doFilter_whenSessionTypeDoesNotMatchRealm_shouldReturnUnauthorized() throws Exception {
        RedisSessionService sessionService = mock(RedisSessionService.class);
        AuthenticationFilter filter = new AuthenticationFilter(sessionService, new ObjectMapper().findAndRegisterModules());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(sessionService.find(PrincipalType.USER, "token")).thenReturn(Optional.of(
                new AuthSession(1L, PrincipalType.MERCHANT, 1L, LocalDateTime.now(), 1)));

        filter.doFilter(request, response, mock(FilterChain.class));

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void doFilter_whenSessionIsValid_shouldSetAndFinallyClearContext() throws Exception {
        RedisSessionService sessionService = mock(RedisSessionService.class);
        AuthenticationFilter filter = new AuthenticationFilter(sessionService, new ObjectMapper().findAndRegisterModules());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/profile");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(sessionService.find(PrincipalType.USER, "token")).thenReturn(Optional.of(
                new AuthSession(8L, PrincipalType.USER, null, LocalDateTime.now(), null)));

        filter.doFilter(request, response,
                (servletRequest, servletResponse) -> assertThat(PrincipalContext.require().principalId()).isEqualTo(8L));

        assertThat(PrincipalContext.get()).isNull();
    }
}
