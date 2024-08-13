package com.wootecam.festivals.global.utils;

import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.auth.AuthenticationContext;

public final class AuthenticationUtils {

    private AuthenticationUtils() {
    }

    public static Long getLoginMemberId() {
        return AuthenticationContext.getAuthentication().memberId();
    }

    public static void setMemberAuthenticated(Authentication authentication) {
        AuthenticationContext.setAuthentication(authentication);
    }
}
