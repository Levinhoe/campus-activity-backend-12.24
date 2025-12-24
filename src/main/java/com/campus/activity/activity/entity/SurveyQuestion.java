package com.campus.activity.activity.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "survey_question")
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "question_text", nullable = false, length = 300)
    private String questionText;

    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType;

    @Column(name = "required_flag", nullable = false)
    private Boolean requiredFlag;

    @Column(name = "sort_no", nullable = false)
    private Integer sortNo;
}
