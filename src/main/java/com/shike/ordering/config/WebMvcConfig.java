package com.shike.ordering.config;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
@Configuration @RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final CorsProperties properties;
    private final StorageProperties storageProperties;
    @Override public void addCorsMappings(CorsRegistry registry) {
        if (!properties.allowedOrigins().isEmpty()) registry.addMapping("/api/**")
                .allowedOrigins(properties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Request-Id", "Idempotency-Key")
                .exposedHeaders("X-Request-Id").allowCredentials(false).maxAge(3600);
    }
    @Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(storageProperties.uploadPath()).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }
}
