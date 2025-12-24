package com.campus.activity.activity.repository;

import com.campus.activity.activity.entity.SurveyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplate, Long> {
    Optional<SurveyTemplate> findFirstByEnabledTrueOrderByCreatedAtDesc();
}
