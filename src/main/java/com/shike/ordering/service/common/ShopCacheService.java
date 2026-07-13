package com.shike.ordering.service.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shike.ordering.config.RedisProperties;
import com.shike.ordering.vo.common.ShopPublicVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopCacheService {
    private static final Duration TTL = Duration.ofMinutes(10);
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisProperties redisProperties;

    public Optional<ShopPublicVO> get(Long shopId) {
        try {
            String value = redisTemplate.opsForValue().get(configKey(shopId));
            return value == null ? Optional.empty() : Optional.of(objectMapper.readValue(value, ShopPublicVO.class));
        } catch (RuntimeException | java.io.IOException exception) {
            log.warn("shop cache read failed, shopId={}", shopId);
            return Optional.empty();
        }
    }

    public void put(Long shopId, ShopPublicVO shop) {
        try {
            redisTemplate.opsForValue().set(configKey(shopId), objectMapper.writeValueAsString(shop), TTL);
        } catch (RuntimeException | java.io.IOException exception) {
            log.warn("shop cache write failed, shopId={}", shopId);
        }
    }

    public void evict(Long shopId) {
        try {
            redisTemplate.delete(java.util.List.of(configKey(shopId), statusKey(shopId)));
        } catch (RuntimeException exception) {
            log.warn("shop cache eviction failed, shopId={}", shopId);
        }
    }

    private String configKey(Long shopId) { return redisProperties.keyPrefix() + "shop:config:" + shopId; }
    private String statusKey(Long shopId) { return redisProperties.keyPrefix() + "shop:status:" + shopId; }
}
