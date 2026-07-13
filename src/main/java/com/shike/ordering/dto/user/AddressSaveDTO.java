package com.shike.ordering.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "收货地址保存请求")
public record AddressSaveDTO(
        @Schema(description = "联系人姓名") @NotBlank(message = "联系人不能为空") @Size(max = 64, message = "联系人长度不能超过64位") String contactName,
        @Schema(description = "性别：1先生、2女士；可空") @Min(value = 1, message = "性别只能为1或2") @Max(value = 2, message = "性别只能为1或2") Integer gender,
        @Schema(description = "联系电话") @NotBlank(message = "联系电话不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确") String phone,
        @Schema(description = "所在区域") @NotBlank(message = "所在区域不能为空") @Size(max = 120, message = "所在区域长度不能超过120位") String area,
        @Schema(description = "详细地址") @NotBlank(message = "详细地址不能为空") @Size(max = 255, message = "详细地址长度不能超过255位") String detail,
        @Schema(description = "门牌号") @Size(max = 100, message = "门牌号长度不能超过100位") String houseNumber,
        @Schema(description = "地址标签，如家、公司、学校") @Size(max = 32, message = "地址标签长度不能超过32位") String tag,
        @Schema(description = "是否设为默认地址") @NotNull(message = "默认地址状态不能为空") Boolean isDefault) {
    public AddressSaveDTO {
        contactName = trim(contactName);
        phone = trim(phone);
        area = trim(area);
        detail = trim(detail);
        houseNumber = trim(houseNumber);
        tag = trim(tag);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
