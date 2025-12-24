package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ActivityStatsVO")
public record ActivityStatsVO(
        @Schema(description = "activityId") Long activityId,
        @Schema(description = "capacity") Integer capacity,
        @Schema(description = "enrolledCount") Integer enrolledCount,
        @Schema(description = "isFull") boolean isFull,
        @Schema(description = "enrollStart") String enrollStart,
        @Schema(description = "enrollDeadline") String enrollDeadline,
        @Schema(description = "currentUserRegistered") boolean currentUserRegistered,
        @Schema(description = "serverTime") String serverTime
) {}
