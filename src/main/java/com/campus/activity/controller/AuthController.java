package com.campus.activity.controller;

import com.campus.activity.common.ApiResult;
import com.campus.activity.dto.request.LoginRequest;
import com.campus.activity.dto.request.RegisterRequest;
import com.campus.activity.dto.response.LoginResponse;
import com.campus.activity.dto.response.UserInfoResponse;
import com.campus.activity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResult<Void> register(@RequestBody @Valid RegisterRequest req) {
        authService.register(req);
        return ApiResult.ok();
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return ApiResult.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResult<UserInfoResponse> me(Authentication authentication) {
        // authentication.getName() = account
        return ApiResult.ok(authService.me(authentication.getName()));
    }
}
