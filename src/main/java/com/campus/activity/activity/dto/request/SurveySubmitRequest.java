package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SurveySubmitRequest {
    @NotNull(message = "ratingScore is required")
    @Min(1)
    @Max(5)
    private Integer ratingScore;

    private String suggestionText;
}
