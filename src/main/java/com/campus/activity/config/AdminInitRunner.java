package com.campus.activity.config;

import com.campus.activity.activity.entity.SysRole;
import com.campus.activity.activity.entity.SysUser;
import com.campus.activity.activity.repository.SysRoleRepository;
import com.campus.activity.activity.repository.SysUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class AdminInitRunner implements ApplicationRunner {

    private final SysUserRepository userRepo;
    private final SysRoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.init.enabled:true}")
    private boolean enabled;

    @Value("${app.admin.init.account:admin}")
    private String adminAccount;

    @Value("${app.admin.init.password:admin123}")
    private String adminPassword;

    public AdminInitRunner(SysUserRepository userRepo,
                           SysRoleRepository roleRepo,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;
        if (userRepo.existsByAccount(adminAccount)) return;

        SysRole role = roleRepo.findByRoleCode("ADMIN")
                .orElseGet(() -> {
                    SysRole r = new SysRole();
                    r.setRoleCode("ADMIN");
                    r.setRoleName("ADMIN");
                    r.setRoleDesc("admin role");
                    return roleRepo.save(r);
                });

        SysUser user = new SysUser();
        user.setRole(role);
        user.setAccount(adminAccount);
        user.setPasswordHash(passwordEncoder.encode(adminPassword));
        user.setName("admin");
        user.setStatus((byte) 1);
        user.setNickname("admin");
        user.setCreatedAt(java.time.LocalDateTime.now());
        userRepo.save(user);
    }
}
