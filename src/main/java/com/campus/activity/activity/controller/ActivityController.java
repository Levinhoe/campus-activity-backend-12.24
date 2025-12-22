package com.campus.activity.activity.controller;

import com.campus.activity.activity.dto.response.ActivityDetailResponse;
import com.campus.activity.activity.dto.response.ActivityListItemResponse;
import com.campus.activity.activity.dto.response.MyRegistrationItemResponse;
import com.campus.activity.activity.service.ActivityService;
import com.campus.activity.common.ApiResult;
import com.campus.activity.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ApiResult<Page<ActivityListItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Byte status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "activityId"));
        return ApiResult.ok(activityService.list(keyword, status, pageable));
    }

    @GetMapping("/{id}")
    public ApiResult<ActivityDetailResponse> detail(@PathVariable("id") Long id) {
        Long userId = SecurityUtil.getUserId(); // 你项目里已有 token，建议这里取 uid
        return ApiResult.ok(activityService.detail(id, userId));
    }

    @PostMapping("/{id}/register")
    public ApiResult<Void> register(@PathVariable("id") Long id) {
        Long userId = SecurityUtil.getUserId();
        activityService.register(id, userId);
        return ApiResult.ok();
    }

    @PostMapping("/{id}/cancel")
    public ApiResult<Void> cancel(@PathVariable("id") Long id) {
        Long userId = SecurityUtil.getUserId();
        activityService.cancel(id, userId);
        return ApiResult.ok();
    }

    @GetMapping("/me")
    public ApiResult<List<MyRegistrationItemResponse>> my() {
        Long userId = SecurityUtil.getUserId();
        return ApiResult.ok(activityService.myRegistrations(userId));
    }
}
