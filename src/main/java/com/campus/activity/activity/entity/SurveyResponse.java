package com.campus.activity.activity.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "survey_response",
        uniqueConstraints = @UniqueConstraint(name = "uk_activity_user_survey", columnNames = {"activity_id","user_id"})
)
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "rating_score", nullable = false)
    private Integer ratingScore;

    @Column(name = "suggestion_text", length = 500)
    private String suggestionText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
