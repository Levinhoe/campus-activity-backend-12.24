package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(name = "ActivityReportVO")
public record ActivityReportVO(
        @Schema(description = "activityId") Long activityId,
        @Schema(description = "name") String name,
        @Schema(description = "type") String type,
        @Schema(description = "timeRange") String timeRange,
        @Schema(description = "location") String location,
        @Schema(description = "capacityLimit") Integer capacityLimit,
        @Schema(description = "registrationsTotal") long registrationsTotal,
        @Schema(description = "approvedCount") long approvedCount,
        @Schema(description = "attendanceNormalCount") long attendanceNormalCount,
        @Schema(description = "attendanceLateCount") long attendanceLateCount,
        @Schema(description = "attendanceAbsentCount") long attendanceAbsentCount,
        @Schema(description = "actualParticipantsCount") long actualParticipantsCount,
        @Schema(description = "avgRatingScore") Double avgRatingScore,
        @Schema(description = "ratingDistribution") Map<Integer, Long> ratingDistribution,
        @Schema(description = "suggestionsTop") List<String> suggestionsTop
) {}
