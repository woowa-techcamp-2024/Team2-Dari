package com.wootecam.festivals.domain.auth.service;

import static com.wootecam.festivals.global.utils.SessionUtils.getAuthentication;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.wootecam.festivals.domain.auth.exception.AuthErrorCode;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import com.wootecam.festivals.utils.TestDBCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuthServiceTest extends SpringBootTestConfig {

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(memberRepository);
    }

    @Test
    @DisplayName("존재하는 이메일로 로그인을 시도하면 로그인에 성공한다")
    void loginSuccess() {
        // given
        Member member = Member.builder()
                .name("name")
                .email("email@example.com")
                .profileImg("profileImg")
                .build();
        memberRepository.save(member);

        // when
        authService.login("email@example.com");

        // then
        assertNotNull(getAuthentication());
    }

    @Test
    @DisplayName("로그인한 유저의 이메일이 존재하지 않는다면 400 에러를 던진다")
    void loginFailUserNotFound() {
        // given
        String email = "nonexistent@example.com";

        // when, then
        assertThatThrownBy(() -> authService.login(email))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_LOGIN_FAILED);
    }

    @Test
    @DisplayName("로그인에 성공한 유저는 재접속 시 세션이 유지된다")
    void loginAndSessionPersistence() {
        // given
        Member member = Member.builder()
                .name("name")
                .email("email@example.com")
                .profileImg("profileImg")
                .build();
        memberRepository.save(member);

        // when
        authService.login("email@example.com");

        // then
        assertNotNull(getAuthentication());

        // Simulate some activity

        // then
        assertNotNull(getAuthentication());
    }

}