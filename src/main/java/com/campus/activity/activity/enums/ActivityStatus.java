package com.campus.activity.activity.enums;

import lombok.Getter;

@Getter
public enum ActivityStatus {
    DRAFT((byte)0, "草稿"),
    SIGNUP((byte)1, "报名中"),
    ENDED((byte)2, "已结束"),
    CANCELED((byte)3, "已取消");

    private final byte code;
    private final String label;

    ActivityStatus(byte code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ActivityStatus of(byte code) {
        for (ActivityStatus s : values()) if (s.code == code) return s;
        throw new IllegalArgumentException("Unknown ActivityStatus code: " + code);
    }
}
