package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SurveyQuestionVO")
public record SurveyQuestionVO(
        @Schema(description = "id") Long id,
        @Schema(description = "questionText") String questionText,
        @Schema(description = "questionType") String questionType,
        @Schema(description = "requiredFlag") boolean requiredFlag,
        @Schema(description = "sortNo") Integer sortNo
) {}
