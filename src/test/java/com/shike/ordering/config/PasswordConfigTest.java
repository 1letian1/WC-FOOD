package com.shike.ordering.config;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
class PasswordConfigTest {
    @Test void passwordEncoder_shouldUseSaltAndMatchRawPassword() {
        PasswordEncoder encoder = new PasswordConfig().passwordEncoder();
        String first = encoder.encode("merchant123");
        String second = encoder.encode("merchant123");
        assertThat(first).isNotEqualTo(second);
        assertThat(encoder.matches("merchant123", first)).isTrue();
        assertThat(encoder.matches("wrong123", first)).isFalse();
    }
}
