package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.request.LoginRequest;
import com.campus.activity.activity.dto.request.RegisterRequest;
import com.campus.activity.activity.dto.response.LoginResponse;
import com.campus.activity.activity.dto.response.UserInfoResponse;

public interface AuthService {
    void register(RegisterRequest req);
    LoginResponse login(LoginRequest req);
    UserInfoResponse me(String account);
}
