package com.shike.ordering.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "收货地址")
public record AddressVO(
        @Schema(description = "地址 ID") Long id,
        @Schema(description = "联系人姓名") String contactName,
        @Schema(description = "性别：1先生、2女士") Integer gender,
        @Schema(description = "联系电话") String phone,
        @Schema(description = "所在区域") String area,
        @Schema(description = "详细地址") String detail,
        @Schema(description = "门牌号") String houseNumber,
        @Schema(description = "地址标签") String tag,
        @Schema(description = "是否默认地址") Boolean isDefault) { }
