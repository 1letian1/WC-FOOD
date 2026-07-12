package com.shike.ordering.common.enums;

import lombok.Getter;

@Getter
public enum EnabledStatus {
    DISABLED(0, "暂停"),
    ENABLED(1, "开放");

    private final int code;
    private final String description;

    EnabledStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static EnabledStatus fromBoolean(boolean enabled) {
        return enabled ? ENABLED : DISABLED;
    }
}
