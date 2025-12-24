package com.campus.activity.activity.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SurveyTemplateVO")
public record SurveyTemplateVO(
        @Schema(description = "templateId") Long templateId,
        @Schema(description = "title") String title,
        @Schema(description = "questions") List<SurveyQuestionVO> questions
) {}
