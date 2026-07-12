package com.shike.ordering.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String MDC_KEY = "requestId";
    public static final String HEADER_NAME = "X-Request-Id";
    private static final int REQUEST_ID_BYTES = 16;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = sanitize(request.getHeader(HEADER_NAME));
        if (!StringUtils.hasText(requestId)) {
            byte[] bytes = new byte[REQUEST_ID_BYTES];
            secureRandom.nextBytes(bytes);
            requestId = HexFormat.of().formatHex(bytes);
        }
        MDC.put(MDC_KEY, requestId);
        response.setHeader(HEADER_NAME, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private String sanitize(String value) {
        if (!StringUtils.hasText(value) || value.length() > 64 || !value.matches("[A-Za-z0-9_-]+")) {
            return null;
        }
        return value;
    }
}
