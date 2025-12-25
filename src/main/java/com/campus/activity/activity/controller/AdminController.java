package com.campus.activity.activity.controller;

import com.campus.activity.activity.dto.request.AdminActivityCreateRequest;
import com.campus.activity.activity.dto.request.ActivityStatusRequest;
import com.campus.activity.activity.dto.request.CheckInRequest;
import com.campus.activity.activity.dto.request.CheckOutRequest;
import com.campus.activity.activity.dto.request.AdminRegistrationQueryRequest;
import com.campus.activity.activity.dto.request.RegistrationRejectRequest;
import com.campus.activity.activity.dto.response.AdminActivityCreateResponse;
import com.campus.activity.activity.dto.response.ActivityReportVO;
import com.campus.activity.activity.dto.response.ActivityListItemVO;
import com.campus.activity.activity.dto.response.RegistrationAuditVO;
import com.campus.activity.activity.dto.response.NotificationVO;
import com.campus.activity.activity.service.AdminActivityService;
import com.campus.activity.activity.service.ActivityService;
import com.campus.activity.activity.service.AttendanceService;
import com.campus.activity.activity.service.NotificationService;
import com.campus.activity.common.ApiResult;
import com.campus.activity.common.PageResult;
import com.campus.activity.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
public class AdminController {

    private final AdminActivityService adminService;
    private final AttendanceService attendanceService;
    private final ActivityService activityService;
    private final NotificationService notificationService;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AdminController(AdminActivityService adminService, AttendanceService attendanceService, ActivityService activityService, NotificationService notificationService) {
        this.adminService = adminService;
        this.attendanceService = attendanceService;
        this.activityService = activityService;
        this.notificationService = notificationService;
    }

    @PostMapping("/activities")
    @Operation(summary = "Create activity")
    public ApiResult<AdminActivityCreateResponse> create(@RequestBody @Valid AdminActivityCreateRequest req) {
        Long adminId = SecurityUtil.getUserId();
        return ApiResult.ok(adminService.create(req, adminId));
    }

    @GetMapping("/activities")
    @Operation(summary = "List activities (admin)")
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

    @GetMapping("/notifications")
    @Operation(summary = "Admin notifications")
    public ApiResult<PageResult<NotificationVO>> notifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getUserId();
        return ApiResult.ok(notificationService.list(userId, page, size));
    }

    @DeleteMapping("/notifications/{id}")
    @Operation(summary = "Delete notification")
    public ApiResult<Void> deleteNotification(@PathVariable("id") Long id) {
        Long userId = SecurityUtil.getUserId();
        notificationService.delete(userId, id);
        return ApiResult.ok();
    }

    @RequestMapping(value = "/activities/{id}/status", method = {RequestMethod.PATCH, RequestMethod.POST})
    @Operation(summary = "Update activity status")
    public ApiResult<Void> updateStatus(
            @PathVariable("id") Long activityId,
            @RequestBody @Valid ActivityStatusRequest req
    ) {
        adminService.updateStatus(activityId, req);
        return ApiResult.ok();
    }

    @DeleteMapping("/activities/{id}")
    @Operation(summary = "Delete activity")
    public ApiResult<Void> delete(@PathVariable("id") Long activityId) {
        adminService.deleteActivity(activityId);
        return ApiResult.ok();
    }

    @PostMapping("/activities/{id}/registrations")
    @Operation(summary = "List registrations for audit")
    public ApiResult<PageResult<RegistrationAuditVO>> registrations(
            @PathVariable("id") Long activityId,
            @RequestBody AdminRegistrationQueryRequest req
    ) {
        int page = req.page() == null ? 1 : req.page();
        int size = req.size() == null ? 10 : req.size();
        return ApiResult.ok(adminService.registrations(activityId, page, size, req.status()));
    }

    @PostMapping("/registrations/{id}/approve")
    @Operation(summary = "Approve registration")
    public ApiResult<Void> approve(@PathVariable("id") Long registrationId) {
        adminService.approve(registrationId);
        return ApiResult.ok();
    }

    @PostMapping("/registrations/{id}/reject")
    @Operation(summary = "Reject registration")
    public ApiResult<Void> reject(
            @PathVariable("id") Long registrationId,
            @RequestBody @Valid RegistrationRejectRequest req
    ) {
        adminService.reject(registrationId, req);
        return ApiResult.ok();
    }

    @PostMapping("/activities/{id}/checkin")
    @Operation(summary = "Check in")
    public ApiResult<Void> checkIn(
            @PathVariable("id") Long activityId,
            @RequestBody @Valid CheckInRequest req
    ) {
        attendanceService.checkIn(activityId, req);
        return ApiResult.ok();
    }

    @PostMapping("/activities/{id}/checkout")
    @Operation(summary = "Check out")
    public ApiResult<Void> checkOut(
            @PathVariable("id") Long activityId,
            @RequestBody @Valid CheckOutRequest req
    ) {
        attendanceService.checkOut(activityId, req);
        return ApiResult.ok();
    }

    @GetMapping("/activities/{id}/report")
    @Operation(summary = "Activity report")
    public ApiResult<ActivityReportVO> report(@PathVariable("id") Long activityId) {
        return ApiResult.ok(adminService.report(activityId));
    }

    @GetMapping("/activities/{id}/report/export")
    @Operation(summary = "Export report")
    public ResponseEntity<String> export(@PathVariable("id") Long activityId) {
        ActivityReportVO report = adminService.report(activityId);
        String csv = "activityId,name,type,timeRange,location,capacityLimit,registrationsTotal,approvedCount,attendanceNormalCount,attendanceLateCount,attendanceAbsentCount,actualParticipantsCount,avgRatingScore\n"
                + report.activityId() + ","
                + safe(report.name()) + ","
                + safe(report.type()) + ","
                + safe(report.timeRange()) + ","
                + safe(report.location()) + ","
                + report.capacityLimit() + ","
                + report.registrationsTotal() + ","
                + report.approvedCount() + ","
                + report.attendanceNormalCount() + ","
                + report.attendanceLateCount() + ","
                + report.attendanceAbsentCount() + ","
                + report.actualParticipantsCount() + ","
                + report.avgRatingScore();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", " ");
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
