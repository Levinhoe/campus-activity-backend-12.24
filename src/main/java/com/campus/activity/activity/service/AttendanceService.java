package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.request.CheckInRequest;
import com.campus.activity.activity.dto.request.CheckOutRequest;
import com.campus.activity.activity.dto.request.StudentCheckInRequest;
import com.campus.activity.activity.dto.response.StudentCheckInResponse;
import com.campus.activity.activity.dto.response.StudentCheckInStatusResponse;
import com.campus.activity.activity.entity.Activity;
import com.campus.activity.activity.entity.Attendance;
import com.campus.activity.activity.entity.ActivityRegistration;
import com.campus.activity.activity.enums.CheckStatus;
import com.campus.activity.activity.enums.RegistrationStatus;
import com.campus.activity.activity.repository.ActivityRegistrationRepository;
import com.campus.activity.activity.repository.ActivityRepository;
import com.campus.activity.activity.repository.AttendanceRepository;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final ActivityRepository activityRepo;
    private final ActivityRegistrationRepository regRepo;
    private final AttendanceRepository attendanceRepo;

    @Transactional
    public void checkIn(Long activityId, CheckInRequest req) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        ActivityRegistration reg = regRepo.findByActivityIdAndStudentNo(activityId, req.getStudentNo())
                .orElseThrow(() -> new BizException(42004, "registration not approved"));

        ensureApproved(reg);

        Attendance attendance = attendanceRepo.findByActivityIdAndUserId(activityId, reg.getUserId())
                .orElseGet(() -> {
                    Attendance a = new Attendance();
                    a.setActivityId(activityId);
                    a.setUserId(reg.getUserId());
                    a.setStudentNo(reg.getStudentNo());
                    a.setCreatedAt(LocalDateTime.now());
                    return a;
                });

        CheckStatus checkStatus = parseCheckStatus(req.getCheckStatus());
        attendance.setCheckStatus(checkStatus.getCode());
        if (checkStatus != CheckStatus.ABSENT) {
            attendance.setCheckInTime(LocalDateTime.now());
        }
        attendance.setUpdatedAt(LocalDateTime.now());
        attendanceRepo.save(attendance);
    }

    @Transactional
    public StudentCheckInResponse studentCheckIn(StudentCheckInRequest req, Long userId) {
        Activity activity = activityRepo.findById(req.getActivityId())
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        ensureCheckInWindow(activity);

        ActivityRegistration reg = regRepo.findByActivityIdAndUserId(req.getActivityId(), userId)
                .orElseThrow(() -> new BizException(42004, "registration not approved"));
        ensureApproved(reg);

        if (attendanceRepo.findByActivityIdAndUserId(req.getActivityId(), userId).isPresent()) {
            throw new BizException(42008, "ALREADY_CHECKED_IN");
        }

        CheckStatus checkStatus = parseCheckStatus(req.getStatus());
        Attendance attendance = new Attendance();
        attendance.setActivityId(req.getActivityId());
        attendance.setUserId(userId);
        attendance.setStudentNo(reg.getStudentNo());
        attendance.setCheckStatus(checkStatus.getCode());
        if (checkStatus != CheckStatus.ABSENT) {
            attendance.setCheckInTime(LocalDateTime.now());
        }
        attendance.setCreatedAt(LocalDateTime.now());
        attendance.setUpdatedAt(LocalDateTime.now());
        attendanceRepo.save(attendance);

        return new StudentCheckInResponse(
                attendance.getCheckInTime() == null ? null : attendance.getCheckInTime().toString(),
                checkStatus.name()
        );
    }

    public StudentCheckInStatusResponse studentCheckInStatus(Long activityId, Long userId) {
        Attendance attendance = attendanceRepo.findByActivityIdAndUserId(activityId, userId).orElse(null);
        if (attendance == null || attendance.getCheckStatus() == null) {
            return new StudentCheckInStatusResponse(false, null, null);
        }
        String status = CheckStatus.of(attendance.getCheckStatus()).name();
        String time = attendance.getCheckInTime() == null ? null : attendance.getCheckInTime().toString();
        return new StudentCheckInStatusResponse(true, status, time);
    }

    @Transactional
    public void checkOut(Long activityId, CheckOutRequest req) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));
        if (!Boolean.TRUE.equals(activity.getIsVolunteer())) {
            throw new BizException(43001, "not a volunteer activity");
        }

        ActivityRegistration reg = regRepo.findByActivityIdAndStudentNo(activityId, req.getStudentNo())
                .orElseThrow(() -> new BizException(42004, "registration not approved"));

        ensureApproved(reg);
        Attendance attendance = attendanceRepo.findByActivityIdAndUserId(activityId, reg.getUserId())
                .orElseThrow(() -> new BizException(43001, "check in required"));

        if (attendance.getCheckInTime() == null) {
            throw new BizException(43001, "check in required");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);
        attendance.setDurationMinutes((int) Duration.between(attendance.getCheckInTime(), now).toMinutes());
        attendance.setUpdatedAt(now);
        attendanceRepo.save(attendance);
    }

    private void ensureCheckInWindow(Activity activity) {
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BizException(40002, "check in not allowed");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BizException(40002, "check in not allowed");
        }
    }

    private void ensureApproved(ActivityRegistration reg) {
        if (reg.getStatus() == null || reg.getStatus() != RegistrationStatus.APPROVED.getCode()) {
            throw new BizException(42004, "registration not approved");
        }
    }

    private CheckStatus parseCheckStatus(String status) {
        if (status == null || status.isBlank()) {
            return CheckStatus.NORMAL;
        }
        try {
            return CheckStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BizException(43001, "invalid status");
        }
    }
}
