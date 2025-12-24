package com.campus.activity.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNAUTHORIZED(40001, "unauthorized"),
    FORBIDDEN(40002, "forbidden"),
    BAD_REQUEST(43001, "bad request"),
    USER_NOT_FOUND(41001, "user not found"),
    ACTIVITY_NOT_FOUND(41002, "activity not found"),
    ACTIVITY_STATUS_INVALID(41003, "activity status invalid"),
    REG_DUPLICATE(42001, "registration duplicate"),
    REG_FULL(42002, "registration full"),
    REG_DEADLINE(42003, "registration deadline passed"),
    REG_NOT_APPROVED(42004, "registration not approved"),
    REG_NOT_FOUND(42005, "registration not found"),
    REG_ALREADY_AUDITED(42006, "registration already audited"),
    SURVEY_DUPLICATE(42007, "survey duplicate"),
    FILE_UPLOAD_FAIL(44001, "file upload failed"),
    SYSTEM_ERROR(999, "system error");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
