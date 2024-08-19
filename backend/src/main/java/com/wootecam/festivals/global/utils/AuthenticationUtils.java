package com.wootecam.festivals.global.utils;

import static com.wootecam.festivals.global.utils.SessionUtils.getSession;

import com.wootecam.festivals.global.auth.Authentication;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticationUtils {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationUtils.class);

    private AuthenticationUtils() {
    }

    public static Long getLoginMemberId() {
        return getAuthentication().memberId();
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

        logger.info("Authentication set: {}", authentication);

        session.setAttribute("authentication", authentication);
    }

    public static void invalidateAuthentication() {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute("authentication");
        }
    }
}
