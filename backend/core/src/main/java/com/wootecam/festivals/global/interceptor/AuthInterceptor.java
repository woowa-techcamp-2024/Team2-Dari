package com.wootecam.festivals.global.interceptor;

import com.wootecam.festivals.global.auth.AuthErrorCode;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.utils.AuthenticationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("request uri: {}", request.getRequestURI());
        Authentication authentication = AuthenticationUtils.getAuthentication();
        validateAuthentication(authentication);
        return true;
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null || !isValidAuthentication(authentication)) {

            log.error("Authentication is invalid {}", authentication);

            throw new ApiException(AuthErrorCode.UNAUTHORIZED);
        }
    }

    private boolean isValidAuthentication(Authentication authentication) {
        return authentication.memberId() != null;
    }
}