package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "nickname is required")
    private String nickname;

    private String avatarUrl;
}
