package com.shike.ordering.common.result;

import java.util.List;

public record PageResult<T>(List<T> records, Long total, Long current, Long size, Long pages) {
    public PageResult { records = records == null ? List.of() : List.copyOf(records); }
}
