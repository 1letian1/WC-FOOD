package com.shike.ordering.dto.common;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopQueryDTOTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_whenShopIdIsNotPositive_shouldFail() {
        assertThat(validator.validate(new ShopQueryDTO(0L)))
                .extracting(violation -> violation.getMessage())
                .containsExactly("店铺ID必须为正数");
    }
}
