package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "拒单请求")
public record OrderRejectDTO(
        @Schema(description = "拒单原因", example = "商品售罄")
        @NotBlank(message = "拒单原因不能为空") @Size(max = 255, message = "拒单原因长度不能超过255位") String reason) {
    public OrderRejectDTO {
        if (reason != null) reason = reason.trim();
    }
}
