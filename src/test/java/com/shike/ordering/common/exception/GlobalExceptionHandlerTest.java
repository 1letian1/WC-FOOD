package com.shike.ordering.common.exception;
import com.shike.ordering.common.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    @Test void handleBusiness_shouldKeepStableCodeAndHttpStatus() {
        ResponseEntity<Result<Void>> response = handler.handleBusiness(new UnauthorizedException());
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(20001);
    }
}
