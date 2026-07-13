package com.shike.ordering.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "微信快捷登录请求")
public record WechatLoginDTO(
        @NotBlank(message = "微信登录 code 不能为空")
        @Size(max = 128, message = "微信登录 code 长度不能超过128位")
        @Schema(description = "wx.login 返回的一次性 code") String code) { }
