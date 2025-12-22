package com.campus.activity.activity.repository;

import com.campus.activity.activity.entity.ActivityRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long> {

    Optional<ActivityRegistration> findByActivityIdAndUserId(Long activityId, Long userId);

    long countByActivityIdAndStatus(Long activityId, Byte status);

    List<ActivityRegistration> findByUserIdAndStatusOrderBySignedAtDesc(Long userId, Byte status);
}
