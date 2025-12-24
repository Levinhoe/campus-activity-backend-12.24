package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.request.SurveySubmitRequest;
import com.campus.activity.activity.dto.response.SurveyQuestionVO;
import com.campus.activity.activity.dto.response.SurveyTemplateVO;
import com.campus.activity.activity.entity.Activity;
import com.campus.activity.activity.entity.ActivityRegistration;
import com.campus.activity.activity.entity.SurveyQuestion;
import com.campus.activity.activity.entity.SurveyResponse;
import com.campus.activity.activity.entity.SurveyTemplate;
import com.campus.activity.activity.enums.ActivityStatus;
import com.campus.activity.activity.enums.RegistrationStatus;
import com.campus.activity.activity.repository.ActivityRegistrationRepository;
import com.campus.activity.activity.repository.ActivityRepository;
import com.campus.activity.activity.repository.SurveyQuestionRepository;
import com.campus.activity.activity.repository.SurveyResponseRepository;
import com.campus.activity.activity.repository.SurveyTemplateRepository;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyTemplateRepository templateRepo;
    private final SurveyQuestionRepository questionRepo;
    private final SurveyResponseRepository responseRepo;
    private final ActivityRepository activityRepo;
    private final ActivityRegistrationRepository regRepo;

    public SurveyTemplateVO template(Long activityId, Long userId) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        ensureSurveyAllowed(activity, activityId, userId);

        SurveyTemplate template = templateRepo.findFirstByEnabledTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new BizException(43001, "survey template not found"));

        List<SurveyQuestionVO> questions = questionRepo.findByTemplateIdOrderBySortNoAsc(template.getId())
                .stream()
                .map(q -> new SurveyQuestionVO(
                        q.getId(),
                        q.getQuestionText(),
                        q.getQuestionType(),
                        Boolean.TRUE.equals(q.getRequiredFlag()),
                        q.getSortNo()
                ))
                .toList();

        return new SurveyTemplateVO(template.getId(), template.getTitle(), questions);
    }

    @Transactional
    public void submit(Long activityId, Long userId, SurveySubmitRequest req) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        ensureSurveyAllowed(activity, activityId, userId);

        if (responseRepo.findByActivityIdAndUserId(activityId, userId).isPresent()) {
            throw new BizException(42007, "survey duplicate");
        }

        SurveyTemplate template = templateRepo.findFirstByEnabledTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new BizException(43001, "survey template not found"));

        SurveyResponse resp = new SurveyResponse();
        resp.setActivityId(activityId);
        resp.setUserId(userId);
        resp.setTemplateId(template.getId());
        resp.setRatingScore(req.getRatingScore());
        resp.setSuggestionText(req.getSuggestionText());
        resp.setCreatedAt(LocalDateTime.now());
        responseRepo.save(resp);
    }

    private void ensureSurveyAllowed(Activity activity, Long activityId, Long userId) {
        ActivityRegistration reg = regRepo.findByActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new BizException(42005, "registration not found"));
        if (reg.getStatus() == null || reg.getStatus() != RegistrationStatus.APPROVED.getCode()) {
            throw new BizException(42004, "registration not approved");
        }
        if (activity.getStatus() != ActivityStatus.ENDED.getCode()) {
            throw new BizException(43001, "activity not ended");
        }
    }
}
