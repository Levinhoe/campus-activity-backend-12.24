package com.campus.activity.config;

import com.campus.activity.activity.entity.SurveyQuestion;
import com.campus.activity.activity.entity.SurveyTemplate;
import com.campus.activity.activity.repository.SurveyQuestionRepository;
import com.campus.activity.activity.repository.SurveyTemplateRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
public class SurveyInitRunner implements ApplicationRunner {

    private final SurveyTemplateRepository templateRepo;
    private final SurveyQuestionRepository questionRepo;

    public SurveyInitRunner(SurveyTemplateRepository templateRepo, SurveyQuestionRepository questionRepo) {
        this.templateRepo = templateRepo;
        this.questionRepo = questionRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (templateRepo.findFirstByEnabledTrueOrderByCreatedAtDesc().isPresent()) {
            return;
        }
        SurveyTemplate template = new SurveyTemplate();
        template.setTitle("Activity feedback");
        template.setEnabled(true);
        template.setCreatedAt(LocalDateTime.now());
        template = templateRepo.save(template);

        SurveyQuestion q1 = new SurveyQuestion();
        q1.setTemplateId(template.getId());
        q1.setQuestionText("Overall rating");
        q1.setQuestionType("RATING");
        q1.setRequiredFlag(true);
        q1.setSortNo(1);

        SurveyQuestion q2 = new SurveyQuestion();
        q2.setTemplateId(template.getId());
        q2.setQuestionText("Suggestions");
        q2.setQuestionType("TEXT");
        q2.setRequiredFlag(false);
        q2.setSortNo(2);

        questionRepo.saveAll(List.of(q1, q2));
    }
}
