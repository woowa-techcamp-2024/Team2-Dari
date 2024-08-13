package com.wootecam.festivals.global.utils;

import com.wootecam.festivals.global.auth.Authentication;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionUtils {
    public static HttpSession getSession() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest().getSession();
        }
        return null;
    }

    public static void invalidateSession() {
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }
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
