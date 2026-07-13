package com.shike.ordering.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户登录结果")
public record UserLoginVO(
        @Schema(description = "用户端访问 Token") String token,
        @Schema(description = "固定有效期，单位秒") long expiresInSeconds,
        @Schema(description = "用户资料") UserProfileVO user) { }
