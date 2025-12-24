package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ActivityDetailVO")
public record ActivityDetailVO(
        @Schema(description = "id") Long id,
        @Schema(description = "name") String name,
        @Schema(description = "type") String type,
        @Schema(description = "startTime") String startTime,
        @Schema(description = "endTime") String endTime,
        @Schema(description = "location") String location,
        @Schema(description = "posterUrl") String posterUrl,
        @Schema(description = "status") String status,
        @Schema(description = "capacityLimit") Integer capacityLimit,
        @Schema(description = "enrollDeadline") String enrollDeadline,
        @Schema(description = "isVolunteer") boolean isVolunteer,
        @Schema(description = "description") String description,
        @Schema(description = "creatorName") String creatorName
) {}
