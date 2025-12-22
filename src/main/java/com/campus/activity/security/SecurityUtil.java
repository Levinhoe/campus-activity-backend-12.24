package com.campus.activity.security;

import com.campus.activity.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class SecurityUtil {

    private SecurityUtil() {}

    public static Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BizException(401, "未登录");

        Object details = auth.getDetails();
        if (!(details instanceof Map<?, ?> map)) {
            throw new BizException(401, "未登录或用户信息缺失");
        }

        Object uid = map.get("uid");
        if (uid == null) {
            throw new BizException(401, "未登录或uid缺失");
        }

        return Long.valueOf(uid.toString());
    }
}
