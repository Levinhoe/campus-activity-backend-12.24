package com.campus.activity.activity.enums;

import lombok.Getter;

@Getter
public enum ActivityStatus {
    NOT_STARTED((byte) 0, "not started"),
    ENROLLING((byte) 1, "enrolling"),
    ENDED((byte) 2, "ended");

    private final byte code;
    private final String label;

    ActivityStatus(byte code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ActivityStatus of(byte code) {
        for (ActivityStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown ActivityStatus code: " + code);
    }
}
