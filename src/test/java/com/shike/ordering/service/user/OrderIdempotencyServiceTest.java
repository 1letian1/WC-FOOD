package com.shike.ordering.service.user;

import com.shike.ordering.config.RedisProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderIdempotencyServiceTest {
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final OrderIdempotencyService service = new OrderIdempotencyService(
            redisTemplate, new RedisProperties("test:"));

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void claim_shouldAtomicallySetProcessingValueWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        assertThat(service.claim(12L, "idem-key-001")).isTrue();

        verify(valueOperations).setIfAbsent("test:order:submit:idempotency:12:idem-key-001",
                "PROCESSING", Duration.ofHours(24));
    }

    @Test
    void transactionCompletion_shouldPublishOrderNumberOnlyAfterCommit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        TransactionSynchronizationManager.initSynchronization();
        service.registerCompletion(12L, "idem-key-001", () -> "2026071400000001");

        TransactionSynchronization synchronization = TransactionSynchronizationManager.getSynchronizations().getFirst();
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
        synchronization.afterCommit();

        verify(valueOperations).set("test:order:submit:idempotency:12:idem-key-001",
                "2026071400000001", Duration.ofHours(24));
    }

    @Test
    void transactionRollback_shouldReleaseProcessingValue() {
        TransactionSynchronizationManager.initSynchronization();
        service.registerCompletion(12L, "idem-key-001", () -> null);

        TransactionSynchronization synchronization = TransactionSynchronizationManager.getSynchronizations().getFirst();
        synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(redisTemplate).delete("test:order:submit:idempotency:12:idem-key-001");
    }
}
