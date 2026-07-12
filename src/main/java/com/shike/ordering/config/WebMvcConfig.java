package com.shike.ordering.config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration @RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final CorsProperties properties;
    @Override public void addCorsMappings(CorsRegistry registry) {
        if (!properties.allowedOrigins().isEmpty()) registry.addMapping("/api/**")
                .allowedOrigins(properties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Request-Id", "Idempotency-Key")
                .exposedHeaders("X-Request-Id").allowCredentials(false).maxAge(3600);
    }
}
