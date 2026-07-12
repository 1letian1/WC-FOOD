package com.shike.ordering.common.result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import static org.assertj.core.api.Assertions.assertThat;
class ResultTest {
    @AfterEach void clearMdc() { MDC.clear(); }
    @Test void success_shouldIncludeRequestIdAndTimestamp() {
        MDC.put("requestId", "request-1");
        Result<String> result = Result.success("data");
        assertThat(result.code()).isZero();
        assertThat(result.requestId()).isEqualTo("request-1");
        assertThat(result.timestamp()).isNotNull();
    }
}
