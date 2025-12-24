package com.campus.activity.activity.controller;

import com.campus.activity.activity.dto.response.ActivityDetailVO;
import com.campus.activity.activity.dto.response.ActivityListItemVO;
import com.campus.activity.activity.service.ActivityService;
import com.campus.activity.common.ApiResult;
import com.campus.activity.common.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Activities")
public class ActivityController {

    private final ActivityService activityService;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @GetMapping
    @Operation(summary = "List activities")
    public ApiResult<PageResult<ActivityListItemVO>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String timeFrom,
            @RequestParam(required = false) String timeTo,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "activityId"));
        LocalDateTime from = parseDateTime(timeFrom);
        LocalDateTime to = parseDateTime(timeTo);
        return ApiResult.ok(activityService.list(type, from, to, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get activity detail")
    public ApiResult<ActivityDetailVO> detail(@PathVariable("id") Long id) {
        return ApiResult.ok(activityService.detail(id));
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(value, DT_FMT);
        }
    }
}
