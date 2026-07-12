package com.shike.ordering.auth.filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shike.ordering.auth.model.AuthSession;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.auth.service.RedisSessionService;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
@Component @Order(Ordered.HIGHEST_PRECEDENCE + 10) @RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private final RedisSessionService sessionService;
    private final ObjectMapper objectMapper;
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/user/") && !uri.startsWith("/api/merchant/")
                || uri.equals("/api/user/auth/wechat-login") || uri.equals("/api/merchant/auth/login");
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        PrincipalType type = request.getRequestURI().startsWith("/api/user/") ? PrincipalType.USER : PrincipalType.MERCHANT;
        String token = resolveToken(request.getHeader("Authorization"));
        Optional<AuthSession> session = token == null ? Optional.empty() : sessionService.find(type, token);
        if (session.isEmpty()) { writeUnauthorized(response); return; }
        AuthSession authSession = session.get();
        PrincipalContext.set(new CurrentPrincipal(authSession.principalId(), authSession.principalType(), authSession.shopId()));
        try { chain.doFilter(request, response); } finally { PrincipalContext.clear(); }
    }
    private String resolveToken(String header) {
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) return null;
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() || token.length() > 256 ? null : token;
    }
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.failure(ErrorCode.UNAUTHORIZED));
    }
}
