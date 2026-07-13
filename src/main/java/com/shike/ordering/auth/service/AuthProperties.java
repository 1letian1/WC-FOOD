package com.shike.ordering.auth.service;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
@ConfigurationProperties("shike.auth")
public record AuthProperties(Duration userSessionTtl, Duration merchantSessionTtl,
                             Duration merchantFailureWindow, int merchantMaxFailures,
                             Duration merchantLockTtl) {
    public AuthProperties {
        if (userSessionTtl == null) userSessionTtl = Duration.ofDays(7);
        if (merchantSessionTtl == null) merchantSessionTtl = Duration.ofHours(12);
        if (merchantFailureWindow == null) merchantFailureWindow = Duration.ofMinutes(15);
        if (merchantMaxFailures <= 0) merchantMaxFailures = 5;
        if (merchantLockTtl == null) merchantLockTtl = Duration.ofMinutes(30);
    }
}
