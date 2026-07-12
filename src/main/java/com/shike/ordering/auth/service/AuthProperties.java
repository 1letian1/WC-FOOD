package com.shike.ordering.auth.service;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
@ConfigurationProperties("shike.auth")
public record AuthProperties(Duration userSessionTtl, Duration merchantSessionTtl) {
    public AuthProperties {
        if (userSessionTtl == null) userSessionTtl = Duration.ofDays(7);
        if (merchantSessionTtl == null) merchantSessionTtl = Duration.ofHours(12);
    }
}
