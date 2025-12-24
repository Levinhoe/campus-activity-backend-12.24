package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ActivityRegistrantVO")
public record ActivityRegistrantVO(
        @Schema(description = "userId") Long userId,
        @Schema(description = "nickname") String nickname,
        @Schema(description = "avatar") String avatar,
        @Schema(description = "registrationTime") String registrationTime
) {}
