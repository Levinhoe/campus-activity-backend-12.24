package com.campus.activity.activity.service.impl;

import com.campus.activity.common.ErrorCode;
import com.campus.activity.activity.dto.request.LoginRequest;
import com.campus.activity.activity.dto.request.RegisterRequest;
import com.campus.activity.activity.dto.response.LoginResponse;
import com.campus.activity.activity.dto.response.UserInfoResponse;
import com.campus.activity.activity.entity.SysRole;
import com.campus.activity.activity.entity.SysUser;
import com.campus.activity.exception.BizException;
import com.campus.activity.activity.repository.SysRoleRepository;
import com.campus.activity.activity.repository.SysUserRepository;
import com.campus.activity.security.JwtTokenProvider;
import com.campus.activity.activity.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository userRepo;
    private final SysRoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;

    public AuthServiceImpl(SysUserRepository userRepo,
                           SysRoleRepository roleRepo,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwt) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    @Override
    @Transactional
    public void register(RegisterRequest req) {
        if (userRepo.existsByAccount(req.getAccount())) {
            throw new BizException(409, "account already exists");
        }
        if (req.getStudentNo() != null && !req.getStudentNo().isBlank()
                && userRepo.existsByStudentNo(req.getStudentNo())) {
            throw new BizException(409, "studentNo already exists");
        }

        String roleCode = "STUDENT";
        if (req.getRole() != null && !req.getRole().isBlank()) {
            roleCode = req.getRole().trim().toUpperCase();
        } else if (req.getStudentNo() == null || req.getStudentNo().isBlank()) {
            roleCode = "ADMIN";
        }

        if (!roleCode.equals("STUDENT") && !roleCode.equals("ADMIN")) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "role is invalid");
        }

        SysRole role = getOrCreateRole(roleCode);

        SysUser user = new SysUser();
        user.setRole(role);
        user.setAccount(req.getAccount());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setStudentNo(req.getStudentNo());
        user.setPhone(req.getPhone());
        user.setStatus((byte) 1);
        user.setNickname(req.getName());
        user.setCreatedAt(java.time.LocalDateTime.now());

        userRepo.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        SysUser user = userRepo.findByAccount(req.getAccount())
                .orElseThrow(() -> new BizException(401, "account or password error"));

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ErrorCode.FORBIDDEN.getCode(), "account disabled");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            if (isLegacyPlainPassword(user.getPasswordHash())
                    && req.getPassword().equals(user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
                userRepo.save(user);
            } else {
                throw new BizException(401, "account or password error");
            }
        }

        String roleCode = user.getRole().getRoleCode();
        String token = jwt.generateToken(user.getAccount(), roleCode, user.getUserId());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserProfile(new com.campus.activity.activity.dto.response.ProfileVO(
                user.getUserId(),
                user.getAccount(),
                user.getNickname(),
                user.getAvatarUrl(),
                roleCode
        ));
        return resp;
    }

    @Override
    public UserInfoResponse me(String account) {
        SysUser user = userRepo.findByAccount(account)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND.getCode(), "user not found"));

        UserInfoResponse info = new UserInfoResponse();
        info.setUserId(user.getUserId());
        info.setAccount(user.getAccount());
        info.setName(user.getName());
        info.setRoleCode(user.getRole().getRoleCode());
        return info;
    }

    private SysRole getOrCreateRole(String roleCode) {
        return roleRepo.findByRoleCode(roleCode)
                .orElseGet(() -> {
                    SysRole role = new SysRole();
                    role.setRoleCode(roleCode);
                    role.setRoleName(roleCode);
                    role.setRoleDesc(roleCode + " role");
                    return roleRepo.save(role);
                });
    }

    private boolean isLegacyPlainPassword(String stored) {
        if (stored == null || stored.isBlank()) {
            return true;
        }
        return !(stored.startsWith("$2a$")
                || stored.startsWith("$2b$")
                || stored.startsWith("$2y$"));
    }
}
