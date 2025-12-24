package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrationRejectRequest {
    @NotBlank(message = "reason is required")
    private String reason;
}
