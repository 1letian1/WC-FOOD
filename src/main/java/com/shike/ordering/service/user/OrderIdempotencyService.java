package com.shike.ordering.service.user;

import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.config.RedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderIdempotencyService {
    private static final String PROCESSING = "PROCESSING";
    private static final Duration TTL = Duration.ofHours(24);
    private final StringRedisTemplate redisTemplate;
    private final RedisProperties redisProperties;

    public boolean claim(Long userId, String idempotencyKey) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(key(userId, idempotencyKey), PROCESSING, TTL));
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.REDIS_UNAVAILABLE);
        }
    }

    public void registerCompletion(Long userId, String idempotencyKey, Supplier<String> orderNoSupplier) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Order idempotency requires an active transaction");
        }
        String redisKey = key(userId, idempotencyKey);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    redisTemplate.opsForValue().set(redisKey, orderNoSupplier.get(), TTL);
                } catch (RuntimeException exception) {
                    log.error("order idempotency completion failed, userId={}", userId);
                }
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_ROLLED_BACK) return;
                try {
                    redisTemplate.delete(redisKey);
                } catch (RuntimeException exception) {
                    log.error("order idempotency rollback cleanup failed, userId={}", userId);
                }
            }
        });
    }

    private String key(Long userId, String idempotencyKey) {
        return redisProperties.keyPrefix() + "order:submit:idempotency:" + userId + ":" + idempotencyKey;
    }
}
