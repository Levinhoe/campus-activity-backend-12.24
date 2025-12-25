package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.request.AdminActivityCreateRequest;
import com.campus.activity.activity.dto.request.ActivityStatusRequest;
import com.campus.activity.activity.dto.request.RegistrationRejectRequest;
import com.campus.activity.activity.dto.response.ActivityReportVO;
import com.campus.activity.activity.dto.response.AdminActivityCreateResponse;
import com.campus.activity.activity.dto.response.RegistrationAuditVO;
import com.campus.activity.activity.entity.Activity;
import com.campus.activity.activity.entity.ActivityRegistration;
import com.campus.activity.activity.entity.Attendance;
import com.campus.activity.activity.enums.ActivityStatus;
import com.campus.activity.activity.enums.CheckStatus;
import com.campus.activity.activity.enums.RegistrationStatus;
import com.campus.activity.activity.repository.ActivityRegistrationRepository;
import com.campus.activity.activity.repository.ActivityRepository;
import com.campus.activity.activity.repository.AttendanceRepository;
import com.campus.activity.activity.repository.SurveyResponseRepository;
import com.campus.activity.common.PageResult;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminActivityService {

    private final ActivityRepository activityRepo;
    private final ActivityRegistrationRepository regRepo;
    private final AttendanceRepository attendanceRepo;
    private final SurveyResponseRepository surveyResponseRepo;
    private final NotificationService notificationService;

    @Value("${app.default-poster:/uploads/default-poster.png}")
    private String defaultPoster;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<DateTimeFormatter> ALT_DT_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    );
    private static final List<DateTimeFormatter> ALT_DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    @Transactional
    public AdminActivityCreateResponse create(AdminActivityCreateRequest req, Long adminId) {
        Activity activity = new Activity();
        activity.setTitle(req.getName());
        activity.setCategory(req.getType());
        activity.setStartTime(parseDateTime(req.getStartTime()));
        activity.setEndTime(parseDateTime(req.getEndTime()));
        activity.setLocation(req.getLocation());
        activity.setCapacity(normalizeCapacity(req.getCapacity()));
        activity.setEnrollDeadline(parseDateTime(req.getDeadline()));
        activity.setEnrollStart(LocalDateTime.now());
        activity.setCoverUrl(req.getPosterUrl() == null || req.getPosterUrl().isBlank() ? defaultPoster : req.getPosterUrl());
        activity.setStatus(ActivityStatus.ENROLLING.getCode());
        activity.setIsVolunteer(false);
        activity.setCreatedBy(adminId);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        activity.setEnrolledCount(0);
        activity.setDescription(req.getDescription());
        activityRepo.save(activity);

        return new AdminActivityCreateResponse(
                activity.getActivityId(),
                activity.getTitle(),
                activity.getCategory(),
                activity.getLocation(),
                activity.getCapacity(),
                activity.getStartTime() == null ? null : activity.getStartTime().toString(),
                activity.getEndTime() == null ? null : activity.getEndTime().toString(),
                activity.getEnrollDeadline() == null ? null : activity.getEnrollDeadline().toString(),
                activity.getCoverUrl(),
                activity.getDescription()
        );
    }

    @Transactional
    public void updateStatus(Long activityId, ActivityStatusRequest req) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));
        ActivityStatus status = parseStatus(req.getStatus());
        activity.setStatus(status.getCode());
        activity.setUpdatedAt(LocalDateTime.now());
        activityRepo.save(activity);
    }

    public PageResult<RegistrationAuditVO> registrations(Long activityId, int page, int size, String status) {
        if (!activityRepo.existsById(activityId)) {
            throw new BizException(41002, "activity not found");
        }
        Page<ActivityRegistration> regPage;
        if (status == null || status.isBlank()) {
            regPage = regRepo.findByActivityIdOrderByCreatedAtDesc(activityId, PageRequest.of(page - 1, size));
        } else {
            RegistrationStatus st = parseRegStatus(status);
            regPage = regRepo.findByActivityIdAndStatusOrderByCreatedAtDesc(
                    activityId, st.getCode(), PageRequest.of(page - 1, size));
        }
        List<RegistrationAuditVO> records = regPage.stream()
                .map(r -> new RegistrationAuditVO(
                        r.getId(),
                        r.getUserId(),
                        r.getRealName(),
                        r.getStudentNo(),
                        r.getPhone(),
                        RegistrationStatus.of(r.getStatus()).name(),
                        r.getAuditReason(),
                        r.getCreatedAt() == null ? null : r.getCreatedAt().toString()
                ))
                .toList();
        return PageResult.of(records, regPage.getTotalElements(), page, size);
    }

    @Transactional
    public void approve(Long registrationId) {
        ActivityRegistration reg = regRepo.findById(registrationId)
                .orElseThrow(() -> new BizException(42005, "registration not found"));
        if (reg.getStatus() == null || reg.getStatus() != RegistrationStatus.PENDING.getCode()) {
            throw new BizException(42006, "registration already audited");
        }

        Activity activity = activityRepo.findByIdForUpdate(reg.getActivityId())
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        Integer capacity = activity.getCapacity();
        if (capacity != null && capacity > 0) {
            long approvedCount = regRepo.countByActivityIdAndStatus(activity.getActivityId(), RegistrationStatus.APPROVED.getCode());
            if (approvedCount >= capacity) {
                throw new BizException(42002, "registration full");
            }
        }

        reg.setStatus(RegistrationStatus.APPROVED.getCode());
        reg.setAuditReason(null);
        reg.setUpdatedAt(LocalDateTime.now());
        regRepo.save(reg);

        Integer current = activity.getEnrolledCount() == null ? 0 : activity.getEnrolledCount();
        activity.setEnrolledCount(current + 1);
        activity.setUpdatedAt(LocalDateTime.now());
        activityRepo.save(activity);

        String activityTitle = activity.getTitle() == null ? "" : activity.getTitle();
        notificationService.notifyRegistration(
                reg.getUserId(),
                activity.getActivityId(),
                activityTitle,
                true,
                null
        );
    }

    @Transactional
    public void reject(Long registrationId, RegistrationRejectRequest req) {
        ActivityRegistration reg = regRepo.findById(registrationId)
                .orElseThrow(() -> new BizException(42005, "registration not found"));
        if (reg.getStatus() == null || reg.getStatus() != RegistrationStatus.PENDING.getCode()) {
            throw new BizException(42006, "registration already audited");
        }
        reg.setStatus(RegistrationStatus.REJECTED.getCode());
        reg.setAuditReason(req.getReason());
        reg.setUpdatedAt(LocalDateTime.now());
        regRepo.save(reg);

        Activity activity = activityRepo.findById(reg.getActivityId()).orElse(null);
        String activityTitle = activity == null ? "" : activity.getTitle();
        notificationService.notifyRegistration(
                reg.getUserId(),
                reg.getActivityId(),
                activityTitle,
                false,
                req.getReason()
        );
    }

    public ActivityReportVO report(Long activityId) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        long approvedCount = regRepo.countByActivityIdAndStatus(activityId, RegistrationStatus.APPROVED.getCode());
        long pendingCount = regRepo.countByActivityIdAndStatus(activityId, RegistrationStatus.PENDING.getCode());
        long rejectedCount = regRepo.countByActivityIdAndStatus(activityId, RegistrationStatus.REJECTED.getCode());
        long canceledCount = regRepo.countByActivityIdAndStatus(activityId, RegistrationStatus.CANCELED.getCode());
        long registrationsTotal = approvedCount + pendingCount + rejectedCount + canceledCount;

        List<Attendance> attendanceList = attendanceRepo.findByActivityId(activityId);
        long normalCount = attendanceList.stream().filter(a -> a.getCheckStatus() == CheckStatus.NORMAL.getCode()).count();
        long lateCount = attendanceList.stream().filter(a -> a.getCheckStatus() == CheckStatus.LATE.getCode()).count();
        long absentCount = attendanceList.stream().filter(a -> a.getCheckStatus() == CheckStatus.ABSENT.getCode()).count();
        long actualParticipants = attendanceList.stream().filter(a -> a.getCheckInTime() != null).count();

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        List<com.campus.activity.activity.entity.SurveyResponse> responses = surveyResponseRepo.findByActivityId(activityId);
        for (int i = 1; i <= 5; i++) {
            int score = i;
            ratingDistribution.put(i, responses.stream().filter(r -> r.getRatingScore() == score).count());
        }
        Double avgRating = responses.isEmpty()
                ? null
                : responses.stream().mapToInt(com.campus.activity.activity.entity.SurveyResponse::getRatingScore).average().orElse(0);
        List<String> suggestionsTop = responses.stream()
                .map(com.campus.activity.activity.entity.SurveyResponse::getSuggestionText)
                .filter(s -> s != null && !s.isBlank())
                .limit(10)
                .toList();

        String timeRange = format(activity.getStartTime()) + " - " + format(activity.getEndTime());

        return new ActivityReportVO(
                activity.getActivityId(),
                activity.getTitle(),
                activity.getCategory(),
                timeRange,
                activity.getLocation(),
                activity.getCapacity(),
                registrationsTotal,
                approvedCount,
                normalCount,
                lateCount,
                absentCount,
                actualParticipants,
                avgRating,
                ratingDistribution,
                suggestionsTop
        );
    }

    @Transactional
    public void deleteActivity(Long activityId) {
        if (!activityRepo.existsById(activityId)) {
            throw new BizException(41002, "activity not found");
        }
        regRepo.deleteByActivityId(activityId);
        attendanceRepo.deleteByActivityId(activityId);
        surveyResponseRepo.deleteByActivityId(activityId);
        activityRepo.deleteById(activityId);
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;

        if (trimmed.matches("^\\d{10}$")) {
            long seconds = Long.parseLong(trimmed);
            return LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(seconds), java.time.ZoneId.systemDefault());
        }
        if (trimmed.matches("^\\d{13}$")) {
            long millis = Long.parseLong(trimmed);
            return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), java.time.ZoneId.systemDefault());
        }

        for (DateTimeFormatter fmt : ALT_DT_FORMATS) {
            try {
                return LocalDateTime.parse(trimmed, fmt);
            } catch (Exception ignored) {
            }
        }
        for (DateTimeFormatter fmt : ALT_DATE_FORMATS) {
            try {
                return java.time.LocalDate.parse(trimmed, fmt).atStartOfDay();
            } catch (Exception ignored) {
            }
        }
        try {
            return LocalDateTime.parse(trimmed);
        } catch (Exception ignored) {
        }
        try {
            return java.time.OffsetDateTime.parse(trimmed).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return java.time.ZonedDateTime.parse(trimmed).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.ofInstant(java.time.Instant.parse(trimmed), java.time.ZoneId.systemDefault());
        } catch (Exception ignored) {
        }
        throw new BizException(43001, "invalid datetime");
    }

    private ActivityStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new BizException(43001, "status is required");
        }
        try {
            return ActivityStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BizException(43001, "invalid status");
        }
    }

    private RegistrationStatus parseRegStatus(String status) {
        try {
            return RegistrationStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BizException(43001, "invalid registration status");
        }
    }

    private Integer normalizeCapacity(Integer capacity) {
        if (capacity == null) return null;
        return capacity <= 0 ? null : capacity;
    }

    private String format(LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }
}
