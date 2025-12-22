package com.campus.activity.activity.dto.response;

public record ActivityListItemResponse(
        Long activityId,
        String title,
        String category,
        String location,
        String startTime,
        String signupEnd,
        Integer capacity,
        Integer signedCount,
        Byte status
) {}
