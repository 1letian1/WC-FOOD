package com.shike.ordering.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("shike.redis")
public record RedisProperties(String keyPrefix) {
    public RedisProperties { if (keyPrefix == null || keyPrefix.isBlank()) keyPrefix = "shike:"; }
}
