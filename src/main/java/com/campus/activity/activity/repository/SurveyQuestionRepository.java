package com.campus.activity.activity.repository;

import com.campus.activity.activity.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
    List<SurveyQuestion> findByTemplateIdOrderBySortNoAsc(Long templateId);
}
