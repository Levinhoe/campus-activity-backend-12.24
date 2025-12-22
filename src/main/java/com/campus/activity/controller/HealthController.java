package com.campus.activity.controller;

import com.campus.activity.common.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResult<String> health() {
        return ApiResult.ok("ok");
    }
}
