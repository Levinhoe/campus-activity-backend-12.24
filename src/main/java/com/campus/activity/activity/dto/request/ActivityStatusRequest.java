package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityStatusRequest {
    @NotBlank(message = "status is required")
    private String status;
}
