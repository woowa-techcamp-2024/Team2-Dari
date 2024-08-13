package com.wootecam.festivals.domain.auth.service;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.exception.UserErrorCode;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
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
        //TODO OAuth 가 연결되지 않았기 때문에 비밀번호 매칭시 항상 로그인 성공으로 처리
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));

        AuthenticationUtils.setMemberAuthenticated(Authentication.from(member));
    }
}
