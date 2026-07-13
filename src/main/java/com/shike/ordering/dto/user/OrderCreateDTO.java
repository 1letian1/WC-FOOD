package com.shike.ordering.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Schema(description = "创建订单请求；金额、用户和店铺均由服务端确定")
public record OrderCreateDTO(
        @Schema(description = "订单类型：1堂食、2配送") @NotNull(message = "订单类型不能为空")
        @Min(value = 1, message = "订单类型只能为1或2") @Max(value = 2, message = "订单类型只能为1或2") Integer orderType,
        @Schema(description = "本次结算的购物车条目 ID") @NotEmpty(message = "购物车不能为空")
        @Size(max = 99, message = "单次结算条目不能超过99个")
        List<@NotNull(message = "购物车条目ID不能为空") @Positive(message = "购物车条目ID必须为正数") Long> cartItemIds,
        @Schema(description = "堂食联系人；配送时不传") @Size(max = 64, message = "联系人长度不能超过64位") String contactName,
        @Schema(description = "堂食联系电话；配送时不传")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确") String contactPhone,
        @Schema(description = "堂食桌号；暂未入座或配送时不传") @Size(max = 32, message = "桌号长度不能超过32位") String tableNo,
        @Schema(description = "是否暂未入座；配送时必须为false") @NotNull(message = "暂未入座状态不能为空") Boolean noSeatYet,
        @Schema(description = "本人配送地址 ID；堂食时不传") @Positive(message = "地址ID必须为正数") Long addressId,
        @Schema(description = "订单备注") @Size(max = 500, message = "订单备注长度不能超过500位") String remark) {

    public OrderCreateDTO {
        cartItemIds = cartItemIds == null ? null
                : Collections.unmodifiableList(new ArrayList<>(cartItemIds));
        contactName = trimToNull(contactName);
        contactPhone = trimToNull(contactPhone);
        tableNo = trimToNull(tableNo);
        remark = trimToNull(remark);
    }

    @AssertTrue(message = "购物车条目不能重复")
    public boolean isCartItemSelectionValid() {
        return cartItemIds == null || new HashSet<>(cartItemIds).size() == cartItemIds.size();
    }

    @AssertTrue(message = "订单履约信息不完整或与订单类型不匹配")
    public boolean isFulfillmentValid() {
        if (orderType == null || noSeatYet == null) return true;
        if (orderType == 1) {
            boolean seatValid = noSeatYet ? tableNo == null : tableNo != null;
            return contactName != null && contactPhone != null && addressId == null && seatValid;
        }
        if (orderType == 2) {
            return addressId != null && contactName == null && contactPhone == null
                    && tableNo == null && !noSeatYet;
        }
        return true;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
