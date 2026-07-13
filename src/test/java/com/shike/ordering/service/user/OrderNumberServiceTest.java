package com.shike.ordering.service.user;

import com.shike.ordering.config.RedisProperties;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderNumberServiceTest {
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final OrderNumberService service = new OrderNumberService(redisTemplate, new RedisProperties("test:"));

    @Test
    void nextOrderNo_shouldUseDailyAtomicSequenceAndEightDigitPadding() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString())).thenReturn(12L);

        String orderNo = service.nextOrderNo();

        String today = LocalDate.now(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.BASIC_ISO_DATE);
        assertThat(orderNo).isEqualTo(today + "00000012");
        verify(redisTemplate).execute(any(DefaultRedisScript.class),
                eq(List.of("test:order:no:" + today)), eq("259200"));
    }
}
