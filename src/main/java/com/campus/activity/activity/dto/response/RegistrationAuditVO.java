package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegistrationAuditVO")
public record RegistrationAuditVO(
        @Schema(description = "registrationId") Long registrationId,
        @Schema(description = "userId") Long userId,
        @Schema(description = "realName") String realName,
        @Schema(description = "studentNo") String studentNo,
        @Schema(description = "phone") String phone,
        @Schema(description = "status") String status,
        @Schema(description = "auditReason") String auditReason,
        @Schema(description = "createdAt") String createdAt
) {}
