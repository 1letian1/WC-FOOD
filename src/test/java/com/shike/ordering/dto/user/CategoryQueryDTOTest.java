package com.shike.ordering.dto.user;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryQueryDTOTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_whenShopIdIsNull_shouldFail() {
        assertThat(validator.validate(new CategoryQueryDTO(null)))
                .extracting(violation -> violation.getMessage())
                .containsExactly("店铺ID不能为空");
    }
}
