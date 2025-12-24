package com.campus.activity.activity.enums;

import lombok.Getter;

@Getter
public enum RegistrationStatus {
    PENDING((byte) 0, "pending"),
    APPROVED((byte) 1, "approved"),
    REJECTED((byte) 2, "rejected"),
    CANCELED((byte) 3, "canceled");

    private final byte code;
    private final String label;

    RegistrationStatus(byte code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RegistrationStatus of(byte code) {
        for (RegistrationStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown RegistrationStatus code: " + code);
    }
}
