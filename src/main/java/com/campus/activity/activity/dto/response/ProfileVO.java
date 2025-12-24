package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProfileVO")
public record ProfileVO(
        @Schema(description = "id") Long id,
        @Schema(description = "username") String username,
        @Schema(description = "nickname") String nickname,
        @Schema(description = "avatarUrl") String avatarUrl,
        @Schema(description = "role") String role
) {}
