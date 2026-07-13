package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "商家修改密码请求")
public record ChangePasswordDTO(
        @NotBlank(message = "当前密码不能为空") @Size(max = 20, message = "当前密码长度不能超过20位")
        @Schema(description = "当前密码") String currentPassword,
        @NotBlank(message = "新密码不能为空")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)\\S{8,20}$",
                message = "新密码须为8至20位且至少包含字母和数字")
        @Schema(description = "新密码，8至20位且至少包含字母和数字") String newPassword) { }
