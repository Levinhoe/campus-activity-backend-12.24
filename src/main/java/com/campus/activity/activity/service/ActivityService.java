package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.response.ActivityDetailResponse;
import com.campus.activity.activity.dto.response.ActivityListItemResponse;
import com.campus.activity.activity.dto.response.MyRegistrationItemResponse;
import com.campus.activity.activity.entity.Activity;
import com.campus.activity.activity.entity.ActivityRegistration;
import com.campus.activity.activity.repository.ActivityRegistrationRepository;
import com.campus.activity.activity.repository.ActivityRepository;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepo;
    private final ActivityRegistrationRepository regRepo;

    public Page<ActivityListItemResponse> list(String keyword, Byte status, Pageable pageable) {
        // Pro：支持筛选。先简单实现：如果你要更复杂可加 Specification。
        Page<Activity> page = activityRepo.findAll(pageable);

        return page.map(a -> new ActivityListItemResponse(
                a.getActivityId(),
                a.getTitle(),
                a.getCategory(),
                a.getLocation(),
                a.getStartTime().toString(),
                a.getSignupEnd().toString(),
                a.getCapacity(),
                a.getSignedCount(),
                a.getStatus()
        ));
    }

    public ActivityDetailResponse detail(Long activityId, Long userId) {
        Activity a = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(404, "活动不存在"));

        boolean signedByMe = regRepo.findByActivityIdAndUserId(activityId, userId)
                .map(r -> r.getStatus() != null && r.getStatus() == (byte) 1)
                .orElse(false);

        return new ActivityDetailResponse(
                a.getActivityId(),
                a.getTitle(),
                a.getCategory(),
                a.getLocation(),
                a.getDescription(),
                a.getCoverUrl(),
                a.getStartTime().toString(),
                a.getEndTime().toString(),
                a.getSignupStart().toString(),
                a.getSignupEnd().toString(),
                a.getCapacity(),
                a.getSignedCount(),
                a.getStatus(),
                signedByMe
        );
    }

    @Transactional
    public void register(Long activityId, Long userId) {
        // 1) 锁活动行（防并发超额）
        Activity a = activityRepo.findByIdForUpdate(activityId)
                .orElseThrow(() -> new BizException(404, "活动不存在"));

        // 2) 校验状态
        if (a.getStatus() == null || a.getStatus() != (byte) 1) {
            throw new BizException(400, "当前活动不可报名");
        }

        // 3) 校验报名时间窗口
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(a.getSignupStart()) || now.isAfter(a.getSignupEnd())) {
            throw new BizException(400, "不在报名时间范围内");
        }

        // 4) 是否已报名（唯一约束 + 逻辑双保险）
        ActivityRegistration reg = regRepo.findByActivityIdAndUserId(activityId, userId).orElse(null);
        if (reg != null && reg.getStatus() != null && reg.getStatus() == (byte) 1) {
            throw new BizException(400, "你已报名，无需重复报名");
        }

        // 5) 校验名额（使用 signed_count 更稳）
        if (a.getSignedCount() >= a.getCapacity()) {
            throw new BizException(400, "名额已满");
        }

        // 6) 写报名记录（存在则恢复，否则新增）
        if (reg == null) {
            reg = new ActivityRegistration();
            reg.setActivityId(activityId);
            reg.setUserId(userId);
        }
        reg.setStatus((byte) 1);
        reg.setSignedAt(LocalDateTime.now());
        reg.setCanceledAt(null);
        regRepo.save(reg);

        // 7) 占用名额
        a.setSignedCount(a.getSignedCount() + 1);
        activityRepo.save(a);
    }

    @Transactional
    public void cancel(Long activityId, Long userId) {
        Activity a = activityRepo.findByIdForUpdate(activityId)
                .orElseThrow(() -> new BizException(404, "活动不存在"));

        ActivityRegistration reg = regRepo.findByActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new BizException(400, "你未报名，无法取消"));

        if (reg.getStatus() == null || reg.getStatus() != (byte) 1) {
            throw new BizException(400, "当前状态不可取消");
        }

        reg.setStatus((byte) 2);
        reg.setCanceledAt(LocalDateTime.now());
        regRepo.save(reg);

        // 释放名额：避免负数保护
        a.setSignedCount(Math.max(0, a.getSignedCount() - 1));
        activityRepo.save(a);
    }

    public List<MyRegistrationItemResponse> myRegistrations(Long userId) {
        List<ActivityRegistration> regs =
                regRepo.findByUserIdAndStatusOrderBySignedAtDesc(userId, (byte) 1);

        // Pro：一次性查活动信息，避免 N+1（这里简单写：逐个查；更严谨可 join 查询）
        return regs.stream()
                .map(r -> {
                    Activity a = activityRepo.findById(r.getActivityId()).orElse(null);
                    if (a == null) return null;
                    return new MyRegistrationItemResponse(
                            a.getActivityId(),
                            a.getTitle(),
                            a.getStartTime().toString(),
                            a.getLocation(),
                            r.getStatus(),
                            r.getSignedAt().toString()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
