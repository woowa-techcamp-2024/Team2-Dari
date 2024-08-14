package com.wootecam.festivals.global.utils;

import static com.wootecam.festivals.global.utils.SessionUtils.getSession;

import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.auth.AuthenticationContext;
import jakarta.servlet.http.HttpSession;

public final class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static Long getLoginMemberId() {
        return AuthenticationContext.getAuthentication().memberId();
    }

    public static Authentication getAuthentication() {
        HttpSession session = getSession();
        if (session != null) {
            return (Authentication) session.getAttribute("authentication");
        }
        return null;
    }

    public static void setAuthenticated(Authentication authentication) {
        HttpSession session = getSession();
        if (session == null) {
            throw new IllegalStateException("session이 존재하지 않습니다.");
        }
        session.setAttribute("authentication", authentication);
    }
}
