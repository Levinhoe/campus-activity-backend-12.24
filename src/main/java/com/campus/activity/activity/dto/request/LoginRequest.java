package com.campus.activity.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "account is required")
    private String account;

    @NotBlank(message = "password is required")
    private String password;
}
