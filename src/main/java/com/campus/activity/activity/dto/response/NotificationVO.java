package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NotificationVO")
public record NotificationVO(
        @Schema(description = "id") Long id,
        @Schema(description = "title") String title,
        @Schema(description = "content") String content,
        @Schema(description = "readFlag") boolean readFlag,
        @Schema(description = "createdAt") String createdAt
) {}
