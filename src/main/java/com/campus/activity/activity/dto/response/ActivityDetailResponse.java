package com.campus.activity.activity.dto.response;

public record ActivityDetailResponse(
        Long activityId,
        String title,
        String category,
        String location,
        String description,
        String coverUrl,
        String startTime,
        String endTime,
        String signupStart,
        String signupEnd,
        Integer capacity,
        Integer signedCount,
        Byte status,
        boolean signedByMe
) {}
