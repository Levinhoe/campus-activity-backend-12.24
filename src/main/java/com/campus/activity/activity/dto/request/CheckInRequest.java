package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequest {
    @NotBlank(message = "studentNo is required")
    private String studentNo;

    @NotBlank(message = "checkStatus is required")
    private String checkStatus;
}
