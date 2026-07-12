package com.shike.ordering.auth.service;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.assertj.core.api.Assertions.assertThat;
class TokenGeneratorTest {
    private final TokenGenerator generator = new TokenGenerator();
    @Test void generate_shouldReturnAtLeast256BitsAndDifferentValues() {
        String first = generator.generate();
        String second = generator.generate();
        assertThat(first).isNotEqualTo(second);
        assertThat(Base64.getUrlDecoder().decode(first)).hasSize(32);
    }
}
