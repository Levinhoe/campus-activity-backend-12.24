package com.campus.activity.activity.dto.request;

public record AdminRegistrationQueryRequest(
        Integer page,
        Integer size,
        String status
) {}
