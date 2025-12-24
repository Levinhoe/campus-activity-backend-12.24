package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckOutRequest {
    @NotBlank(message = "studentNo is required")
    private String studentNo;
}
