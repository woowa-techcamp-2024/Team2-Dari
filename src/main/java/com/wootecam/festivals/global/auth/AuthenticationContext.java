package com.wootecam.festivals.global.auth;

public final class AuthenticationContext {

    private static final ThreadLocal<Authentication> AUTHENTICATION = new ThreadLocal<>();

    private AuthenticationContext() {
    }

    public static Authentication getAuthentication() {
        return AUTHENTICATION.get();
    }

    public static void setAuthentication(Authentication authentication) {
        AUTHENTICATION.set(authentication);
    }

    public static void clear() {
        AUTHENTICATION.remove();
    }
}
