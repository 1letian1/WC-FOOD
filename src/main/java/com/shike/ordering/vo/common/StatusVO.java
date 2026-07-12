package com.shike.ordering.vo.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "状态信息")
public record StatusVO(
        @Schema(description = "稳定状态编码", example = "1") Integer code,
        @Schema(description = "状态中文说明", example = "营业中") String description) {
}
