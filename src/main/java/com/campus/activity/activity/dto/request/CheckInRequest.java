package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonAlias;

@Data
public class CheckInRequest {
    @NotBlank(message = "studentNo is required")
    private String studentNo;

    @JsonAlias({"status"})
    private String checkStatus;
}
