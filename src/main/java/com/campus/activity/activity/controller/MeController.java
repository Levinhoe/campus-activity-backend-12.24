package com.campus.activity.activity.controller;

import com.campus.activity.activity.dto.request.PasswordUpdateRequest;
import com.campus.activity.activity.dto.request.ProfileUpdateRequest;
import com.campus.activity.activity.dto.response.NotificationVO;
import com.campus.activity.activity.dto.response.ProfileVO;
import com.campus.activity.activity.service.NotificationService;
import com.campus.activity.activity.service.UserService;
import com.campus.activity.common.ApiResult;
import com.campus.activity.common.PageResult;
import com.campus.activity.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Me")
public class MeController {

    private final UserService userService;
    private final NotificationService notificationService;

    public MeController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get profile")
    public ApiResult<ProfileVO> profile() {
        Long userId = SecurityUtil.getUserId();
        return ApiResult.ok(userService.profile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update profile")
    public ApiResult<Void> updateProfile(@RequestBody @Valid ProfileUpdateRequest req) {
        Long userId = SecurityUtil.getUserId();
        userService.updateProfile(userId, req);
        return ApiResult.ok();
    }

    @PutMapping("/password")
    @Operation(summary = "Update password")
    public ApiResult<Void> updatePassword(@RequestBody @Valid PasswordUpdateRequest req) {
        Long userId = SecurityUtil.getUserId();
        userService.updatePassword(userId, req);
        return ApiResult.ok();
    }

    @GetMapping("/notifications")
    @Operation(summary = "List notifications")
    public ApiResult<PageResult<NotificationVO>> notifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getUserId();
        return ApiResult.ok(notificationService.list(userId, page, size));
    }

    @PostMapping("/notifications/{id}/read")
    @Operation(summary = "Mark notification read")
    public ApiResult<Void> read(@PathVariable("id") Long id) {
        Long userId = SecurityUtil.getUserId();
        notificationService.markRead(userId, id);
        return ApiResult.ok();
    }
}
