package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NotificationVO")
public record NotificationVO(
        @Schema(description = "id") Long id,
        @Schema(description = "title") String title,
        @Schema(description = "content") String content,
        @Schema(description = "activityId") Long activityId,
        @Schema(description = "activityTitle") String activityTitle,
        @Schema(description = "activityName") String activityName,
        @Schema(description = "name") String name,
        @Schema(description = "ratingScore") Integer ratingScore,
        @Schema(description = "suggestion") String suggestion,
        @Schema(description = "type") String type,
        @Schema(description = "readFlag") boolean readFlag,
        @Schema(description = "createdAt") String createdAt
) {}
