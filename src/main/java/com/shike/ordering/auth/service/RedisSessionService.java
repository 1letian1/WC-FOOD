package com.shike.ordering.auth.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shike.ordering.auth.model.AuthSession;
import com.shike.ordering.auth.model.PrincipalType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Optional;
@Service @RequiredArgsConstructor
public class RedisSessionService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthKeyFactory keyFactory;
    private final AuthProperties properties;
    public void save(String token, AuthSession session) {
        Duration ttl = session.principalType() == PrincipalType.USER ? properties.userSessionTtl() : properties.merchantSessionTtl();
        redisTemplate.opsForValue().set(keyFactory.sessionKey(session.principalType(), token), toJson(session), ttl);
    }
    public Optional<AuthSession> find(PrincipalType type, String token) {
        String json = redisTemplate.opsForValue().get(keyFactory.sessionKey(type, token));
        if (json == null) return Optional.empty();
        try { return Optional.of(objectMapper.readValue(json, AuthSession.class)); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Invalid authentication session data", exception); }
    }
    public void delete(PrincipalType type, String token) { redisTemplate.delete(keyFactory.sessionKey(type, token)); }
    private String toJson(AuthSession session) {
        try { return objectMapper.writeValueAsString(session); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Authentication session serialization failed", exception); }
    }
}
