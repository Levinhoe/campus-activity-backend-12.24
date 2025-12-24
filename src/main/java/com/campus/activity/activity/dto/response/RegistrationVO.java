package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegistrationVO")
public record RegistrationVO(
        @Schema(description = "activityId") Long activityId,
        @Schema(description = "userId") Long userId,
        @Schema(description = "status") String status,
        @Schema(description = "auditReason") String auditReason,
        @Schema(description = "createdAt") String createdAt
) {}
