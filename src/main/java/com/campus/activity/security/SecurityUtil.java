package com.campus.activity.security;

import com.campus.activity.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class SecurityUtil {

    private SecurityUtil() {}

    public static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BizException(40001, "unauthorized");

        Object details = auth.getDetails();
        if (!(details instanceof Map<?, ?> map)) {
            throw new BizException(40001, "unauthorized");
        }

        Object uid = map.get("uid");
        if (uid == null) {
            throw new BizException(40001, "unauthorized");
        }

        return Long.valueOf(uid.toString());
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
