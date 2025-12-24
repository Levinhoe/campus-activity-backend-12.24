package com.campus.activity.security;

import com.campus.activity.activity.entity.SysUser;
import com.campus.activity.activity.repository.SysUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserRepository userRepo;

    public CustomUserDetailsService(SysUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepo.findByAccount(username)
                .orElseThrow(() -> new UsernameNotFoundException("account not found"));

        String roleCode = user.getRole() == null ? null : user.getRole().getRoleCode();
        String normalized = roleCode == null ? "USER" : roleCode.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;

        return new org.springframework.security.core.userdetails.User(
                user.getAccount(),
                user.getPasswordHash(),
                enabled,
                true, true, true,
                List.of(new SimpleGrantedAuthority(normalized))
        );
    }
}
