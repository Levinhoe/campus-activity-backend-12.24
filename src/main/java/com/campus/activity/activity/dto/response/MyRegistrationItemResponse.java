package com.campus.activity.activity.dto.response;

public record MyRegistrationItemResponse(
        Long activityId,
        String title,
        String startTime,
        String location,
        Byte regStatus,
        String signedAt
) {}
