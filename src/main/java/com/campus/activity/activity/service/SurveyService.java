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
import com.campus.activity.activity.repository.SysUserRepository;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyTemplateRepository templateRepo;
    private final SurveyQuestionRepository questionRepo;
    private final SurveyResponseRepository responseRepo;
    private final ActivityRepository activityRepo;
    private final ActivityRegistrationRepository regRepo;
    private final SysUserRepository userRepo;
    private final NotificationService notificationService;

    public SurveyTemplateVO template(Long activityId, Long userId) {
        Activity activity = activityRepo.findById(activityId)
                .orElseThrow(() -> new BizException(41002, "activity not found"));

        ensureSurveyAllowed(activity, activityId, userId);

        SurveyTemplate template = templateRepo.findFirstByEnabledTrueOrderByCreatedAtDesc()
                .orElse(null);

        if (template == null) {
            return defaultTemplate();
        }

        List<SurveyQuestionVO> questions = questionRepo.findByTemplateIdOrderBySortNoAsc(template.getId())
                .stream()
                .map(this::toQuestionVO)
                .toList();

        return new SurveyTemplateVO(template.getId(), template.getTitle(), null, questions);
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
                .orElseGet(this::createDefaultTemplate);

        SurveyResponse resp = new SurveyResponse();
        resp.setActivityId(activityId);
        resp.setUserId(userId);
        resp.setTemplateId(template.getId());
        resp.setRatingScore(req.getRatingScore());
        resp.setSuggestionText(req.getSuggestionText());
        resp.setCreatedAt(LocalDateTime.now());
        responseRepo.save(resp);

        notifyAdmins(activity, userId, req.getRatingScore(), req.getSuggestionText());
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

    private SurveyTemplateVO defaultTemplate() {
        List<SurveyQuestionVO> questions = List.of(
                new SurveyQuestionVO(
                        "score",
                        "rating",
                        "综合满意度",
                        5,
                        true,
                        null,
                        null
                ),
                new SurveyQuestionVO(
                        "suggestion",
                        "textarea",
                        "改进建议",
                        null,
                        false,
                        "填写想法/建议",
                        500
                )
        );
        return new SurveyTemplateVO(
                null,
                "活动满意度调查",
                "请对本次活动给出评分与建议",
                questions
        );
    }

    private SurveyTemplate createDefaultTemplate() {
        SurveyTemplate template = new SurveyTemplate();
        template.setTitle("活动满意度调查");
        template.setEnabled(true);
        template.setCreatedAt(LocalDateTime.now());
        template = templateRepo.save(template);

        SurveyQuestion q1 = new SurveyQuestion();
        q1.setTemplateId(template.getId());
        q1.setQuestionText("综合满意度");
        q1.setQuestionType("RATING");
        q1.setRequiredFlag(true);
        q1.setSortNo(1);

        SurveyQuestion q2 = new SurveyQuestion();
        q2.setTemplateId(template.getId());
        q2.setQuestionText("改进建议");
        q2.setQuestionType("TEXTAREA");
        q2.setRequiredFlag(false);
        q2.setSortNo(2);

        questionRepo.saveAll(List.of(q1, q2));
        return template;
    }

    private SurveyQuestionVO toQuestionVO(SurveyQuestion q) {
        String type = normalizeType(q.getQuestionType());
        Integer scale = "rating".equals(type) ? 5 : null;
        Integer maxLength = "textarea".equals(type) ? 500 : null;
        return new SurveyQuestionVO(
                q.getId() == null ? null : q.getId().toString(),
                type,
                q.getQuestionText(),
                scale,
                Boolean.TRUE.equals(q.getRequiredFlag()),
                null,
                maxLength
        );
    }

    private void notifyAdmins(Activity activity, Long userId, Integer ratingScore, String suggestion) {
        List<com.campus.activity.activity.entity.SysUser> admins = userRepo.findByRoleRoleCode("ADMIN");
        if (admins.isEmpty()) {
            return;
        }
        for (com.campus.activity.activity.entity.SysUser admin : admins) {
            notificationService.notifyFeedback(admin.getUserId(), activity.getActivityId(), ratingScore, suggestion);
        }
    }

    private String normalizeType(String type) {
        if (type == null) return "text";
        String t = type.trim().toLowerCase(Locale.ROOT);
        if (t.equals("rating")) return "rating";
        if (t.equals("textarea") || t.equals("text")) return "textarea";
        if (t.equals("suggestion")) return "textarea";
        return t;
    }
}
