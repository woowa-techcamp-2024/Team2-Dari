package com.wootecam.festivals.domain.auth.service;

import static com.wootecam.festivals.global.utils.AuthenticationUtils.setAuthenticated;
import static com.wootecam.festivals.global.utils.SessionUtils.invalidateSession;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.auth.AuthErrorCode;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;

    public void login(String email) {
        Authentication authentication = AuthenticationUtils.getAuthentication();
        if (authentication != null) {
            throw new ApiException(AuthErrorCode.ALREADY_LOGIN);
        }

        //TODO OAuth 가 연결되지 않았기 때문에 비밀번호 매칭시 항상 로그인 성공으로 처리
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(AuthErrorCode.USER_LOGIN_FAILED));

        // session 저장
        setAuthenticated(Authentication.from(member.getId()));
    }

    public void logout() {
        invalidateSession();
    }
}
