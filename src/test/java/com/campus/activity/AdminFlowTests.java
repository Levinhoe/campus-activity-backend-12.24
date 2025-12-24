package com.campus.activity;

import com.campus.activity.activity.dto.request.AdminActivityCreateRequest;
import com.campus.activity.activity.dto.request.CheckInRequest;
import com.campus.activity.activity.dto.request.CheckOutRequest;
import com.campus.activity.activity.dto.request.RegistrationCreateRequest;
import com.campus.activity.activity.dto.request.RegistrationRejectRequest;
import com.campus.activity.activity.dto.request.SurveySubmitRequest;
import com.campus.activity.activity.entity.Activity;
import com.campus.activity.activity.entity.ActivityRegistration;
import com.campus.activity.activity.entity.SurveyQuestion;
import com.campus.activity.activity.entity.SurveyTemplate;
import com.campus.activity.activity.enums.ActivityStatus;
import com.campus.activity.activity.enums.CheckStatus;
import com.campus.activity.activity.enums.RegistrationStatus;
import com.campus.activity.activity.repository.ActivityRegistrationRepository;
import com.campus.activity.activity.repository.ActivityRepository;
import com.campus.activity.activity.repository.SurveyQuestionRepository;
import com.campus.activity.activity.repository.SurveyTemplateRepository;
import com.campus.activity.activity.repository.SysRoleRepository;
import com.campus.activity.activity.repository.SysUserRepository;
import com.campus.activity.activity.service.AdminActivityService;
import com.campus.activity.activity.service.AttendanceService;
import com.campus.activity.activity.service.RegistrationService;
import com.campus.activity.activity.service.SurveyService;
import com.campus.activity.activity.entity.SysRole;
import com.campus.activity.activity.entity.SysUser;
import com.campus.activity.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AdminFlowTests {

    @Autowired
    private AdminActivityService adminActivityService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private ActivityRepository activityRepo;
    @Autowired
    private ActivityRegistrationRepository regRepo;
    @Autowired
    private SysUserRepository userRepo;
    @Autowired
    private SysRoleRepository roleRepo;
    @Autowired
    private SurveyTemplateRepository templateRepo;
    @Autowired
    private SurveyQuestionRepository questionRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        regRepo.deleteAll();
        activityRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();
        questionRepo.deleteAll();
        templateRepo.deleteAll();
    }

    @Test
    void approve_full_should_fail() {
        Activity activity = createActivity(1);
        SysUser user1 = createUser("u1");
        SysUser user2 = createUser("u2");

        registrationService.register(activity.getActivityId(), user1.getUserId(), buildReq("S1"));
        BizException ex = assertThrows(BizException.class,
                () -> registrationService.register(activity.getActivityId(), user2.getUserId(), buildReq("S2")));
        assertEquals(42002, ex.getCode());
    }

    @Test
    void checkin_and_checkout() {
        Activity activity = createActivity(1);
        activity.setIsVolunteer(true);
        activityRepo.save(activity);
        SysUser user = createUser("u3");
        registrationService.register(activity.getActivityId(), user.getUserId(), buildReq("S3"));
        ActivityRegistration reg = regRepo.findByActivityIdAndUserId(activity.getActivityId(), user.getUserId()).orElseThrow();
        adminActivityService.approve(reg.getId());

        CheckInRequest checkIn = new CheckInRequest();
        checkIn.setStudentNo("S3");
        checkIn.setCheckStatus(CheckStatus.NORMAL.name());
        attendanceService.checkIn(activity.getActivityId(), checkIn);

        CheckOutRequest checkOut = new CheckOutRequest();
        checkOut.setStudentNo("S3");
        attendanceService.checkOut(activity.getActivityId(), checkOut);
    }

    @Test
    void survey_duplicate() {
        Activity activity = createActivity(0);
        activity.setStatus(ActivityStatus.ENDED.getCode());
        activityRepo.save(activity);
        SysUser user = createUser("u4");
        registrationService.register(activity.getActivityId(), user.getUserId(), buildReq("S4"));
        ActivityRegistration reg = regRepo.findByActivityIdAndUserId(activity.getActivityId(), user.getUserId()).orElseThrow();
        reg.setStatus(RegistrationStatus.APPROVED.getCode());
        regRepo.save(reg);

        SurveyTemplate template = new SurveyTemplate();
        template.setTitle("t");
        template.setEnabled(true);
        template.setCreatedAt(LocalDateTime.now());
        template = templateRepo.save(template);

        SurveyQuestion q = new SurveyQuestion();
        q.setTemplateId(template.getId());
        q.setQuestionText("q");
        q.setQuestionType("RATING");
        q.setRequiredFlag(true);
        q.setSortNo(1);
        questionRepo.save(q);

        SurveySubmitRequest req = new SurveySubmitRequest();
        req.setRatingScore(5);
        req.setSuggestionText("ok");
        surveyService.submit(activity.getActivityId(), user.getUserId(), req);

        BizException ex = assertThrows(BizException.class,
                () -> surveyService.submit(activity.getActivityId(), user.getUserId(), req));
        assertEquals(42007, ex.getCode());
    }

    @Test
    void report_basic() {
        Activity activity = createActivity(2);
        adminActivityService.report(activity.getActivityId());
    }

    private Activity createActivity(int capacity) {
        AdminActivityCreateRequest req = new AdminActivityCreateRequest();
        req.setName("A");
        req.setType("T");
        req.setStartTime("2025-12-30 18:00");
        req.setEndTime("2025-12-30 20:00");
        req.setLocation("L");
        req.setCapacity(capacity);
        req.setDeadline("2025-12-25 18:00");
        req.setPosterUrl(null);
        req.setDescription("D");
        adminActivityService.create(req, 1L);
        return activityRepo.findAll().stream().findFirst().orElseThrow();
    }

    private SysUser createUser(String account) {
        SysRole role = roleRepo.findByRoleCode("STUDENT")
                .orElseGet(() -> {
                    SysRole r = new SysRole();
                    r.setRoleCode("STUDENT");
                    r.setRoleName("STUDENT");
                    r.setRoleDesc("student");
                    return roleRepo.save(r);
                });
        SysUser user = new SysUser();
        user.setRole(role);
        user.setAccount(account);
        user.setPasswordHash(passwordEncoder.encode("pwd"));
        user.setName(account);
        user.setNickname(account);
        user.setStatus((byte) 1);
        user.setCreatedAt(LocalDateTime.now());
        return userRepo.save(user);
    }

    private RegistrationCreateRequest buildReq(String studentNo) {
        RegistrationCreateRequest req = new RegistrationCreateRequest();
        req.setRealName("Name");
        req.setStudentNo(studentNo);
        req.setPhone("13800000000");
        return req;
    }
}
