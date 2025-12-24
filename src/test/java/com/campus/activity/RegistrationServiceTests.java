package com.campus.activity;

import com.campus.activity.activity.dto.request.RegistrationCreateRequest;
import com.campus.activity.activity.entity.Activity;
import com.campus.activity.activity.entity.SysRole;
import com.campus.activity.activity.entity.SysUser;
import com.campus.activity.activity.enums.ActivityStatus;
import com.campus.activity.activity.enums.RegistrationStatus;
import com.campus.activity.activity.repository.ActivityRegistrationRepository;
import com.campus.activity.activity.repository.ActivityRepository;
import com.campus.activity.activity.repository.SysRoleRepository;
import com.campus.activity.activity.repository.SysUserRepository;
import com.campus.activity.activity.service.RegistrationService;
import com.campus.activity.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RegistrationServiceTests {

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private ActivityRepository activityRepo;
    @Autowired
    private ActivityRegistrationRepository regRepo;
    @Autowired
    private SysUserRepository userRepo;
    @Autowired
    private SysRoleRepository roleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        regRepo.deleteAll();
        activityRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();
    }

    @Test
    void register_success() {
        SysUser user = createUser("u1", "STUDENT");
        Activity activity = createActivity(ActivityStatus.ENROLLING.getCode(), null, null, 0);

        registrationService.register(activity.getActivityId(), user.getUserId(), buildReq());

        Activity updated = activityRepo.findById(activity.getActivityId()).orElseThrow();
        assertEquals(0, updated.getEnrolledCount());
        Byte status = regRepo.findByActivityIdAndUserId(activity.getActivityId(), user.getUserId())
                .orElseThrow()
                .getStatus();
        assertEquals(RegistrationStatus.PENDING.getCode(), status);
    }

    @Test
    void register_duplicate() {
        SysUser user = createUser("u2", "STUDENT");
        Activity activity = createActivity(ActivityStatus.ENROLLING.getCode(), null, null, 0);
        registrationService.register(activity.getActivityId(), user.getUserId(), buildReq());

        BizException ex = assertThrows(BizException.class,
                () -> registrationService.register(activity.getActivityId(), user.getUserId(), buildReq()));
        assertEquals(42001, ex.getCode());
    }

    @Test
    void register_deadline_passed() {
        SysUser user = createUser("u4", "STUDENT");
        LocalDateTime now = LocalDateTime.now();
        Activity activity = createActivity(ActivityStatus.ENROLLING.getCode(), now.minusDays(2), now.minusDays(1), 0);

        BizException ex = assertThrows(BizException.class,
                () -> registrationService.register(activity.getActivityId(), user.getUserId(), buildReq()));
        assertEquals(42003, ex.getCode());
    }

    @Test
    void cancel_and_reregister() {
        SysUser user = createUser("u5", "STUDENT");
        Activity activity = createActivity(ActivityStatus.ENROLLING.getCode(), null, null, 0);
        registrationService.register(activity.getActivityId(), user.getUserId(), buildReq());
        registrationService.cancel(activity.getActivityId(), user.getUserId());

        Activity afterCancel = activityRepo.findById(activity.getActivityId()).orElseThrow();
        assertEquals(0, afterCancel.getEnrolledCount());

        registrationService.register(activity.getActivityId(), user.getUserId(), buildReq());
        Activity afterRe = activityRepo.findById(activity.getActivityId()).orElseThrow();
        assertEquals(0, afterRe.getEnrolledCount());
    }

    @Test
    void concurrent_register_no_oversell() throws Exception {
        Activity activity = createActivity(ActivityStatus.ENROLLING.getCode(), null, null, 1);
        SysUser u1 = createUser("u6", "STUDENT");
        SysUser u2 = createUser("u7", "STUDENT");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger success = new AtomicInteger(0);

        Runnable task1 = () -> {
            await(start);
            try {
                registrationService.register(activity.getActivityId(), u1.getUserId(), buildReq());
                success.incrementAndGet();
            } catch (BizException ignored) {
            }
        };
        Runnable task2 = () -> {
            await(start);
            try {
                registrationService.register(activity.getActivityId(), u2.getUserId(), buildReq());
                success.incrementAndGet();
            } catch (BizException ignored) {
            }
        };

        futures.add(executor.submit(task1));
        futures.add(executor.submit(task2));
        start.countDown();
        for (Future<?> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }
        executor.shutdownNow();

        Activity updated = activityRepo.findById(activity.getActivityId()).orElseThrow();
        assertTrue(updated.getEnrolledCount() <= 1);
        assertEquals(1, success.get());
    }

    private SysUser createUser(String account, String roleCode) {
        SysRole role = roleRepo.findByRoleCode(roleCode)
                .orElseGet(() -> {
                    SysRole r = new SysRole();
                    r.setRoleCode(roleCode);
                    r.setRoleName(roleCode);
                    r.setRoleDesc(roleCode + " role");
                    return roleRepo.save(r);
                });

        SysUser user = new SysUser();
        user.setRole(role);
        user.setAccount(account);
        user.setPasswordHash(passwordEncoder.encode("pwd"));
        user.setName(account);
        user.setStatus((byte) 1);
        user.setCreatedAt(LocalDateTime.now());
        return userRepo.save(user);
    }

    private Activity createActivity(int status, LocalDateTime start, LocalDateTime deadline, Integer capacity) {
        Activity activity = new Activity();
        activity.setTitle("A");
        activity.setCategory("C");
        activity.setLocation("L");
        activity.setDescription("D");
        activity.setCoverUrl(null);
        activity.setStartTime(LocalDateTime.now().plusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(2));
        activity.setEnrollStart(start);
        activity.setEnrollDeadline(deadline);
        activity.setStatus((byte) status);
        activity.setCapacity(capacity);
        activity.setEnrolledCount(0);
        activity.setIsVolunteer(false);
        activity.setCreatedBy(1L);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        activity.setVersion(0L);
        return activityRepo.save(activity);
    }

    private RegistrationCreateRequest buildReq() {
        RegistrationCreateRequest req = new RegistrationCreateRequest();
        req.setRealName("Name");
        req.setStudentNo("S" + System.nanoTime());
        req.setPhone("13800000000");
        return req;
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }
}
