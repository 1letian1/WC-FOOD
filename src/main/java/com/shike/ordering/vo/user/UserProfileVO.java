package com.shike.ordering.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户资料")
public record UserProfileVO(
        @Schema(description = "用户 ID") Long id,
        @Schema(description = "用户昵称") String nickname,
        @Schema(description = "头像地址") String avatarUrl,
        @Schema(description = "联系电话") String phone) { }
