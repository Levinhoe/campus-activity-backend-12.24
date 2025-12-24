package com.campus.activity.activity.controller;

import com.campus.activity.activity.dto.request.RegistrationCreateRequest;
import com.campus.activity.activity.dto.request.SurveySubmitRequest;
import com.campus.activity.activity.dto.response.ActivityStatsVO;
import com.campus.activity.activity.dto.response.MyRegistrationVO;
import com.campus.activity.activity.dto.response.RegistrationVO;
import com.campus.activity.activity.dto.response.SurveyTemplateVO;
import com.campus.activity.activity.service.RegistrationService;
import com.campus.activity.activity.service.SurveyService;
import com.campus.activity.common.ApiResult;
import com.campus.activity.common.PageResult;
import com.campus.activity.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Registrations")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final SurveyService surveyService;

    public RegistrationController(RegistrationService registrationService, SurveyService surveyService) {
        this.registrationService = registrationService;
        this.surveyService = surveyService;
    }

    @GetMapping("/me/registrations")
    @Operation(summary = "My registrations (paged)")
    public ApiResult<PageResult<MyRegistrationVO>> myRegistrations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        Long userId = SecurityUtil.getUserId();
        return ApiResult.ok(registrationService.myRegistrations(userId, page, size, status));
    }

    @PostMapping("/activities/{id}/registrations")
    @Operation(summary = "Register activity")
    public ApiResult<RegistrationVO> register(
            @PathVariable("id") Long activityId,
            @RequestBody @Valid RegistrationCreateRequest req
    ) {
        Long userId = SecurityUtil.getUserId();
        return ApiResult.ok(registrationService.register(activityId, userId, req));
    }

    @GetMapping("/activities/{id}/stats")
    @Operation(summary = "Activity stats + current user registered")
    public ApiResult<ActivityStatsVO> stats(@PathVariable("id") Long activityId) {
        Long userId = SecurityUtil.getUserId();
        return ApiResult.ok(registrationService.stats(activityId, userId));
    }

    @PostMapping("/activities/{id}/registrations/cancel")
    @Operation(summary = "Cancel registration")
    public ApiResult<Void> cancel(@PathVariable("id") Long activityId) {
        Long userId = SecurityUtil.getUserId();
        registrationService.cancel(activityId, userId);
        return ApiResult.ok();
    }

    @GetMapping("/activities/{id}/survey/template")
    @Operation(summary = "Get survey template")
    public ApiResult<SurveyTemplateVO> surveyTemplate(@PathVariable("id") Long activityId) {
        Long userId = SecurityUtil.getUserId();
        return ApiResult.ok(surveyService.template(activityId, userId));
    }

    @PostMapping("/activities/{id}/survey/submit")
    @Operation(summary = "Submit survey")
    public ApiResult<Void> submitSurvey(
            @PathVariable("id") Long activityId,
            @RequestBody @Valid SurveySubmitRequest req
    ) {
        Long userId = SecurityUtil.getUserId();
        surveyService.submit(activityId, userId, req);
        return ApiResult.ok();
    }
}
