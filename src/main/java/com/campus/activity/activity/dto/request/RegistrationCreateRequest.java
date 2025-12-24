package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrationCreateRequest {
    @NotBlank(message = "realName is required")
    private String realName;

    @NotBlank(message = "studentNo is required")
    private String studentNo;

    @NotBlank(message = "phone is required")
    private String phone;
}
