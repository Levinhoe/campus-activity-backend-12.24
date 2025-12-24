package com.campus.activity.activity.repository;

import com.campus.activity.activity.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    Optional<SurveyResponse> findByActivityIdAndUserId(Long activityId, Long userId);
    List<SurveyResponse> findByActivityId(Long activityId);
    void deleteByActivityId(Long activityId);
}
