package com.shike.ordering.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "商家登录请求")
public record MerchantLoginDTO(
        @NotBlank(message = "商家账号不能为空") @Size(max = 64, message = "商家账号长度不能超过64位")
        @Schema(description = "商家账号") String username,
        @NotBlank(message = "商家密码不能为空") @Size(max = 20, message = "商家密码长度不能超过20位")
        @Schema(description = "商家密码") String password) { }
