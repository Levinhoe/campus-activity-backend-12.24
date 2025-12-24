package com.campus.activity.activity.repository;

import com.campus.activity.activity.entity.ActivityRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long> {

    Optional<ActivityRegistration> findByActivityIdAndUserId(Long activityId, Long userId);

    Optional<ActivityRegistration> findByActivityIdAndStudentNo(Long activityId, String studentNo);

    Page<ActivityRegistration> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<ActivityRegistration> findByActivityIdAndStatusOrderByCreatedAtDesc(Long activityId, Byte status, Pageable pageable);

    Page<ActivityRegistration> findByActivityIdOrderByCreatedAtDesc(Long activityId, Pageable pageable);

    Page<ActivityRegistration> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Byte status, Pageable pageable);

    long countByActivityIdAndStatus(Long activityId, Byte status);

    long countByActivityIdAndStatusIn(Long activityId, java.util.Collection<Byte> status);

    void deleteByActivityId(Long activityId);
}
