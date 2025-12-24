package com.campus.activity.activity.enums;

import lombok.Getter;

@Getter
public enum CheckStatus {
    NONE((byte) 0, "none"),
    NORMAL((byte) 1, "normal"),
    LATE((byte) 2, "late"),
    ABSENT((byte) 3, "absent");

    private final byte code;
    private final String label;

    CheckStatus(byte code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CheckStatus of(byte code) {
        for (CheckStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown CheckStatus code: " + code);
    }
}
