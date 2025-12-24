package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.response.ActivityDetailVO;
import com.campus.activity.activity.dto.response.ActivityListItemVO;
import com.campus.activity.activity.entity.Activity;
import com.campus.activity.activity.enums.ActivityStatus;
import com.campus.activity.activity.repository.ActivityRepository;
import com.campus.activity.activity.repository.SysUserRepository;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.campus.activity.common.PageResult;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepo;
    private final SysUserRepository userRepo;

    @Value("${app.default-poster:/uploads/default-poster.png}")
    private String defaultPoster;

    public PageResult<ActivityListItemVO> list(
            String type,
            LocalDateTime timeFrom,
            LocalDateTime timeTo,
            String status,
            Pageable pageable
    ) {
        Specification<Activity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("category"), type.trim()));
            }
            if (timeFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), timeFrom));
            }
            if (timeTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), timeTo));
            }
            if (status != null && !status.isBlank()) {
                ActivityStatus st = parseStatus(status);
                predicates.add(cb.equal(root.get("status"), st.getCode()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        var page = activityRepo.findAll(spec, pageable);
        List<ActivityListItemVO> records = page.stream()
                .map(a -> new ActivityListItemVO(
                        a.getActivityId(),
                        a.getTitle(),
                        a.getCategory(),
                        a.getStartTime() == null ? null : a.getStartTime().toString(),
                        a.getEndTime() == null ? null : a.getEndTime().toString(),
                        a.getLocation(),
                        a.getCoverUrl() == null ? defaultPoster : a.getCoverUrl(),
                        ActivityStatus.of(a.getStatus()).name(),
                        a.getCapacity(),
                        a.getEnrollDeadline() == null ? null : a.getEnrollDeadline().toString(),
                        Boolean.TRUE.equals(a.getIsVolunteer())
                ))
                .toList();
        return PageResult.of(records, page.getTotalElements(), pageable.getPageNumber() + 1, pageable.getPageSize());
    }

    public ActivityDetailVO detail(Long activityId) {
        Activity a = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        String creatorName = userRepo.findById(a.getCreatedBy())
                .map(u -> u.getName())
                .orElse(null);

        return new ActivityDetailVO(
                a.getActivityId(),
                a.getTitle(),
                a.getCategory(),
                a.getStartTime() == null ? null : a.getStartTime().toString(),
                a.getEndTime() == null ? null : a.getEndTime().toString(),
                a.getLocation(),
                a.getCoverUrl() == null ? defaultPoster : a.getCoverUrl(),
                ActivityStatus.of(a.getStatus()).name(),
                a.getCapacity(),
                a.getEnrollDeadline() == null ? null : a.getEnrollDeadline().toString(),
                Boolean.TRUE.equals(a.getIsVolunteer()),
                a.getDescription(),
                creatorName
        );
    }

    private ActivityStatus parseStatus(String status) {
        try {
            return ActivityStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BizException(43001, "invalid status");
        }
    }
}
