package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminActivityCreateRequest {
    @NotBlank(message = "name is required")
    private String name;

    private String type;

    @NotBlank(message = "location is required")
    private String location;

    private Integer capacity;

    @NotBlank(message = "startTime is required")
    private String startTime;

    @NotBlank(message = "endTime is required")
    private String endTime;

    @NotBlank(message = "deadline is required")
    private String deadline;

    private String posterUrl;

    private String description;
}
