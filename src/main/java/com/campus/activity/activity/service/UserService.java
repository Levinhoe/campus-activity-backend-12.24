package com.campus.activity.activity.service;

import com.campus.activity.activity.dto.request.PasswordUpdateRequest;
import com.campus.activity.activity.dto.request.ProfileUpdateRequest;
import com.campus.activity.activity.dto.response.ProfileVO;
import com.campus.activity.activity.entity.SysUser;
import com.campus.activity.activity.repository.SysUserRepository;
import com.campus.activity.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-avatar:/uploads/default-avatar.png}")
    private String defaultAvatar;

    public ProfileVO profile(Long userId) {
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException(41001, "user not found"));
        String avatarUrl = user.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isBlank()) {
            avatarUrl = defaultAvatar;
        }
        return new ProfileVO(
                user.getUserId(),
                user.getAccount(),
                user.getNickname(),
                avatarUrl,
                user.getRole().getRoleCode()
        );
    }

    public void updateProfile(Long userId, ProfileUpdateRequest req) {
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException(41001, "user not found"));
        user.setNickname(req.getNickname());
        if (req.getAvatarUrl() != null) {
            user.setAvatarUrl(req.getAvatarUrl());
        }
        userRepo.save(user);
    }

    public void updateAvatar(Long userId, String avatarUrl) {
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException(41001, "user not found"));
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            user.setAvatarUrl(avatarUrl);
            userRepo.save(user);
        }
    }

    public void updatePassword(Long userId, PasswordUpdateRequest req) {
        SysUser user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException(41001, "user not found"));
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new BizException(43001, "old password incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepo.save(user);
    }
}
