package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.request.RegistrationCreateRequest;
import com.campus.activity.activity.dto.response.ActivityStatsVO;
import com.campus.activity.activity.dto.response.MyRegistrationVO;
import com.campus.activity.activity.dto.response.RegistrationVO;
import com.campus.activity.activity.entity.Activity;
import com.campus.activity.activity.entity.ActivityRegistration;
import com.campus.activity.activity.enums.RegistrationStatus;
import com.campus.activity.activity.repository.ActivityRegistrationRepository;
import com.campus.activity.activity.repository.ActivityRepository;
import com.campus.activity.common.PageResult;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final ActivityRepository activityRepo;
    private final ActivityRegistrationRepository regRepo;
    @Value("${app.default-poster:/uploads/default-poster.png}")
    private String defaultPoster;

    @Transactional
    public RegistrationVO register(Long activityId, Long userId, RegistrationCreateRequest req) {
        Activity activity = activityRepo.findByIdForUpdate(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        LocalDateTime now = LocalDateTime.now();
        if (activity.getEnrollDeadline() != null && now.isAfter(activity.getEnrollDeadline())) {
            throw new BizException(42003, "registration deadline passed");
        }

        Integer capacity = activity.getCapacity();
        if (capacity != null && capacity > 0) {
            long count = regRepo.countByActivityIdAndStatusIn(
                    activityId,
                    List.of(RegistrationStatus.PENDING.getCode(), RegistrationStatus.APPROVED.getCode())
            );
            if (count >= capacity) {
                throw new BizException(42002, "registration full");
            }
        }

        ActivityRegistration reg = regRepo.findByActivityIdAndUserId(activityId, userId).orElse(null);
        if (reg != null && reg.getStatus() != null) {
            if (reg.getStatus() == RegistrationStatus.PENDING.getCode()
                    || reg.getStatus() == RegistrationStatus.APPROVED.getCode()) {
                throw new BizException(42001, "registration duplicate");
            }
        }

        if (reg == null) {
            reg = new ActivityRegistration();
            reg.setActivityId(activityId);
            reg.setUserId(userId);
            reg.setCreatedAt(now);
        } else if (reg.getStatus() == RegistrationStatus.CANCELED.getCode()
                || reg.getStatus() == RegistrationStatus.REJECTED.getCode()) {
            reg.setCreatedAt(now);
        }
        reg.setRealName(req.getRealName());
        reg.setStudentNo(req.getStudentNo());
        reg.setPhone(req.getPhone());
        reg.setStatus(RegistrationStatus.PENDING.getCode());
        reg.setAuditReason(null);
        reg.setUpdatedAt(now);
        regRepo.save(reg);

        return new RegistrationVO(
                activityId,
                userId,
                RegistrationStatus.PENDING.name(),
                null,
                now.toString()
        );
    }

    @Transactional
    public void cancel(Long activityId, Long userId) {
        Activity activity = activityRepo.findByIdForUpdate(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        ActivityRegistration reg = regRepo.findByActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new BizException(42005, "registration not found"));

        if (reg.getStatus() == null
                || (reg.getStatus() != RegistrationStatus.PENDING.getCode()
                && reg.getStatus() != RegistrationStatus.APPROVED.getCode())) {
            throw new BizException(42005, "registration not found");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean wasApproved = reg.getStatus() == RegistrationStatus.APPROVED.getCode();
        reg.setStatus(RegistrationStatus.CANCELED.getCode());
        reg.setUpdatedAt(now);
        regRepo.save(reg);

        if (wasApproved) {
            int current = activity.getEnrolledCount() == null ? 0 : activity.getEnrolledCount();
            activity.setEnrolledCount(Math.max(0, current - 1));
            activity.setUpdatedAt(now);
            activityRepo.save(activity);
        }
    }

    public PageResult<MyRegistrationVO> myRegistrations(Long userId, int page, int size, String status) {
        Page<ActivityRegistration> regPage;
        if (status == null || status.isBlank()) {
            regPage = regRepo.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page - 1, size));
        } else {
            RegistrationStatus st;
            try {
                st = RegistrationStatus.valueOf(status.trim().toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new BizException(43001, "invalid registration status");
            }
            regPage = regRepo.findByUserIdAndStatusOrderByCreatedAtDesc(
                    userId, st.getCode(), PageRequest.of(page - 1, size));
        }

        List<Long> activityIds = regPage.stream().map(ActivityRegistration::getActivityId).distinct().toList();
        Map<Long, Activity> activityMap = new HashMap<>();
        if (!activityIds.isEmpty()) {
            activityRepo.findAllById(activityIds).forEach(a -> activityMap.put(a.getActivityId(), a));
        }

        List<MyRegistrationVO> records = regPage.stream()
                .map(reg -> {
                    Activity a = activityMap.get(reg.getActivityId());
                    return new MyRegistrationVO(
                            reg.getActivityId(),
                            a == null ? null : a.getTitle(),
                            a == null ? null : a.getCategory(),
                            a == null || a.getStartTime() == null ? null : a.getStartTime().toString(),
                            a == null ? null : a.getLocation(),
                            a == null ? null : (a.getCoverUrl() == null ? defaultPoster : a.getCoverUrl()),
                            reg.getCreatedAt() == null ? null : reg.getCreatedAt().toString(),
                            RegistrationStatus.of(reg.getStatus()).name(),
                            reg.getAuditReason(),
                            a != null && Boolean.TRUE.equals(a.getIsVolunteer())
                    );
                })
                .toList();

        return PageResult.of(records, regPage.getTotalElements(), page, size);
    }

    public ActivityStatsVO stats(Long activityId, Long userId) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        boolean currentUserRegistered = regRepo.findByActivityIdAndUserId(activityId, userId)
                .map(r -> r.getStatus() != null && r.getStatus() == RegistrationStatus.APPROVED.getCode())
                .orElse(false);

        Integer capacity = activity.getCapacity();
        int enrolled = activity.getEnrolledCount() == null ? 0 : activity.getEnrolledCount();
        boolean isFull = capacity != null && capacity > 0 && enrolled >= capacity;

        return new ActivityStatsVO(
                activity.getActivityId(),
                capacity,
                enrolled,
                isFull,
                activity.getEnrollStart() == null ? null : activity.getEnrollStart().toString(),
                activity.getEnrollDeadline() == null ? null : activity.getEnrollDeadline().toString(),
                currentUserRegistered,
                LocalDateTime.now().toString()
        );
    }
}
