package com.wootecam.festivals.global.interceptor;

import com.wootecam.festivals.domain.auth.exception.AuthErrorCode;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.utils.AuthenticationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = AuthenticationUtils.getAuthentication();
        validateAuthentication(authentication);
        return true;
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null || !isValidAuthentication(authentication)) {
            throw new ApiException(AuthErrorCode.UNAUTHORIZED);
        }
    }

    private boolean isValidAuthentication(Authentication authentication) {
        return memberRepository.findById(authentication.memberId())
                .map(member -> isMatchingMember(authentication, member))
                .orElse(false);
    }

    private boolean isMatchingMember(Authentication authentication, Member member) {
        return authentication.email().equals(member.getEmail())
                && authentication.name().equals(member.getName());
    }
}