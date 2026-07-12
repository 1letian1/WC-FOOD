package com.shike.ordering.auth.service;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.config.RedisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class AuthKeyFactory {
    private final RedisProperties properties;
    public String sessionKey(PrincipalType type, String token) {
        String realm = type == PrincipalType.USER ? "user" : "merchant";
        return properties.keyPrefix() + "auth:" + realm + ":session:" + token;
    }
}
