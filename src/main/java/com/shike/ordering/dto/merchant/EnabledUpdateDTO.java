package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "履约开关请求")
public record EnabledUpdateDTO(
        @NotNull(message = "开关状态不能为空") @Schema(description = "true 开放，false 暂停") Boolean enabled) { }
