package com.campus.activity.activity.enums;

import lombok.Getter;

@Getter
public enum RegistrationStatus {
    SIGNED((byte)1, "已报名"),
    CANCELED((byte)2, "已取消");

    private final byte code;
    private final String label;

    RegistrationStatus(byte code, String label) {
        this.code = code;
        this.label = label;
    }
}
