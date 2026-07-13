package com.shike.ordering.service.user;

import com.shike.ordering.common.exception.BusinessException;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.config.RedisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderNumberService {
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final long MAX_DAILY_SEQUENCE = 99_999_999L;
    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT = new DefaultRedisScript<>(
            "local seq = redis.call('INCR', KEYS[1]); "
                    + "if seq == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; return seq;",
            Long.class);
    private final StringRedisTemplate redisTemplate;
    private final RedisProperties redisProperties;

    public String nextOrderNo() {
        String date = LocalDate.now(ZONE_ID).format(DATE_FORMAT);
        try {
            Long sequence = redisTemplate.execute(INCREMENT_SCRIPT,
                    List.of(redisProperties.keyPrefix() + "order:no:" + date), "259200");
            if (sequence == null || sequence < 1 || sequence > MAX_DAILY_SEQUENCE) {
                throw new BusinessException(ErrorCode.REDIS_UNAVAILABLE);
            }
            return date + String.format("%08d", sequence);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.REDIS_UNAVAILABLE);
        }
    }
}
