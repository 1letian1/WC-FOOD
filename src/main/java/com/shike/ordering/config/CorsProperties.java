package com.shike.ordering.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;
@ConfigurationProperties("shike.cors")
public record CorsProperties(List<String> allowedOrigins) {
    public CorsProperties { allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins); }
}
