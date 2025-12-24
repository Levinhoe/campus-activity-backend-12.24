package com.campus.activity.activity.controller;

import com.campus.activity.common.ApiResult;
import com.campus.activity.activity.dto.request.LoginRequest;
import com.campus.activity.activity.dto.request.RegisterRequest;
import com.campus.activity.activity.dto.response.LoginResponse;
import com.campus.activity.activity.dto.response.UserInfoResponse;
import com.campus.activity.activity.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register")
    public ApiResult<Void> register(@RequestBody @Valid RegisterRequest req) {
        authService.register(req);
        return ApiResult.ok();
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ApiResult<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return ApiResult.ok(authService.login(req));
    }

    @GetMapping("/me")
    @Operation(summary = "Me")
    public ApiResult<UserInfoResponse> me(Authentication authentication) {
        // authentication.getName() = account
        return ApiResult.ok(authService.me(authentication.getName()));
    }
}
