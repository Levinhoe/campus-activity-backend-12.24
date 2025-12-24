package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminActivityCreateResponse")
public record AdminActivityCreateResponse(
        @Schema(description = "id") Long id,
        @Schema(description = "name") String name,
        @Schema(description = "type") String type,
        @Schema(description = "location") String location,
        @Schema(description = "capacity") Integer capacity,
        @Schema(description = "startTime") String startTime,
        @Schema(description = "endTime") String endTime,
        @Schema(description = "deadline") String deadline,
        @Schema(description = "posterUrl") String posterUrl,
        @Schema(description = "description") String description
) {}
