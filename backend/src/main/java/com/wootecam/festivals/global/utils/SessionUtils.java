package com.wootecam.festivals.global.utils;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionUtils {

    private SessionUtils() {
    }

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
}
