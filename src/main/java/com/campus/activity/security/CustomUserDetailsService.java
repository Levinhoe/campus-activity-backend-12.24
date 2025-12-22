package com.campus.activity.security;

import com.campus.activity.activity.entity.SysUser;
import com.campus.activity.activity.repository.SysUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new UsernameNotFoundException("账号不存在"));

        String roleCode = user.getRole().getRoleCode();
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;


        return new org.springframework.security.core.userdetails.User(
                user.getAccount(),
                user.getPasswordHash(),
                enabled,
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + roleCode))
        );
    }
}
