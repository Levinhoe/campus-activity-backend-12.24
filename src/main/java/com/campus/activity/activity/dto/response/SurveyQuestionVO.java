package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SurveyQuestionVO")
public record SurveyQuestionVO(
        @Schema(description = "id") String id,
        @Schema(description = "type") String type,
        @Schema(description = "label") String label,
        @Schema(description = "scale") Integer scale,
        @Schema(description = "required") boolean required,
        @Schema(description = "placeholder") String placeholder,
        @Schema(description = "maxLength") Integer maxLength
) {}
