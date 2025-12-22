package com.campus.activity.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserInfoResponse user;
}
