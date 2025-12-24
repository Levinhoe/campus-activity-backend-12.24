package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MyRegistrationVO")
public record MyRegistrationVO(
        @Schema(description = "activityId") Long activityId,
        @Schema(description = "name") String name,
        @Schema(description = "type") String type,
        @Schema(description = "startTime") String startTime,
        @Schema(description = "location") String location,
        @Schema(description = "posterUrl") String posterUrl,
        @Schema(description = "registrationTime") String registrationTime,
        @Schema(description = "status") String status,
        @Schema(description = "auditReason") String auditReason,
        @Schema(description = "isVolunteer") boolean isVolunteer
) {}
