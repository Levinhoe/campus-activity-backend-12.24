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
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "账号已存在");
        }
        if (req.getStudentNo() != null && !req.getStudentNo().isBlank()
                && userRepo.existsByStudentNo(req.getStudentNo())) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "学号已存在");
        }

        SysRole studentRole = roleRepo.findByRoleCode("STUDENT")
                .orElseThrow(() -> new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "角色STUDENT不存在，请检查sys_role初始化"));

        SysUser user = new SysUser();
        user.setRole(studentRole);
        user.setAccount(req.getAccount());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setStudentNo(req.getStudentNo());
        user.setPhone(req.getPhone());
        user.setStatus((byte) 1);

        userRepo.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        SysUser user = userRepo.findByAccount(req.getAccount())
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_ERROR.getCode(), "账号或密码错误"));

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ErrorCode.FORBIDDEN.getCode(), "账号已被禁用");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.BIZ_ERROR.getCode(), "账号或密码错误");
        }

        String roleCode = user.getRole().getRoleCode();
        String token = jwt.generateToken(user.getAccount(), roleCode, user.getUserId());

        UserInfoResponse info = new UserInfoResponse();
        info.setUserId(user.getUserId());
        info.setAccount(user.getAccount());
        info.setName(user.getName());
        info.setRoleCode(roleCode);

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUser(info);
        return resp;
    }

    @Override
    public UserInfoResponse me(String account) {
        SysUser user = userRepo.findByAccount(account)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND.getCode(), "用户不存在"));

        UserInfoResponse info = new UserInfoResponse();
        info.setUserId(user.getUserId());
        info.setAccount(user.getAccount());
        info.setName(user.getName());
        info.setRoleCode(user.getRole().getRoleCode());
        return info;
    }
}
