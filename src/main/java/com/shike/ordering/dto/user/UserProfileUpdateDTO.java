package com.shike.ordering.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "用户资料修改请求，至少提供一个字段")
public record UserProfileUpdateDTO(
        @Schema(description = "用户昵称")
        @Size(min = 1, max = 64, message = "用户昵称长度必须为1至64位") String nickname,
        @Schema(description = "头像地址")
        @Size(min = 1, max = 500, message = "头像地址长度必须为1至500位") String avatarUrl,
        @Schema(description = "手机号")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {

    public UserProfileUpdateDTO {
        nickname = trim(nickname);
        avatarUrl = trim(avatarUrl);
        phone = trim(phone);
    }

    @AssertTrue(message = "至少提供一项用户资料")
    @Schema(hidden = true)
    public boolean isAnyFieldProvided() {
        return nickname != null || avatarUrl != null || phone != null;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
