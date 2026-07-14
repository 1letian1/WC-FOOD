package com.shike.ordering.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "统一分页结果")
public record PageResult<T>(
        @Schema(description = "当前页记录") List<T> records,
        @Schema(description = "总记录数") Long total,
        @Schema(description = "当前页码") Long current,
        @Schema(description = "每页数量") Long size,
        @Schema(description = "总页数") Long pages) {
    public PageResult { records = records == null ? List.of() : List.copyOf(records); }
}
