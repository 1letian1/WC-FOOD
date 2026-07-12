package com.shike.ordering.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CategoryQueryDTO(
        @NotNull(message = "店铺ID不能为空")
        @Positive(message = "店铺ID必须为正数")
        Long shopId) {
}
