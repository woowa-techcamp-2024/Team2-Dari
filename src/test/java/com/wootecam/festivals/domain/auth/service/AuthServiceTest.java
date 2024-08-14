package com.wootecam.festivals.domain.auth.service;

import static com.wootecam.festivals.global.utils.AuthenticationUtils.getAuthentication;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("AuthService 테스트")
class AuthServiceTest extends SpringBootTestConfig {

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(memberRepository);

        // given
        memberRepository.save(createMember("email@example.com"));
    }

    @Nested
    @DisplayName("로그인 기능")
    class LoginFeature {

        @Test
        @DisplayName("존재하는 이메일로 로그인 시 인증 정보가 생성된다")
        void loginWithExistingEmailCreatesAuthentication() {
            // given

            // when
            authService.login("email@example.com");

            // then
            assertNotNull(getAuthentication());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
        void loginWithNonExistentEmailThrowsException() {
            // given
            String email = "nonexistent@example.com";

            // when, then
            assertThatThrownBy(() -> authService.login(email))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_LOGIN_FAILED);
        }

        @Test
        @DisplayName("로그인 후 인증 정보가 유지된다")
        void authenticationPersistsAfterLogin() {
            // when
            authService.login("email@example.com");

            // then
            assertNotNull(getAuthentication());

            // Simulate some activity

            // then
            assertNotNull(getAuthentication());
        }
    }

    @Nested
    @DisplayName("로그아웃 기능")
    class LogoutFeature {

        @Test
        @DisplayName("로그아웃 시 인증 정보가 제거된다")
        void logoutRemovesAuthentication() {
            // given
            authService.login("email@example.com");

            // when
            authService.logout();

            // then
            assertNull(getAuthentication());
        }
    }

    private Member createMember(String email) {
        return Member.builder()
                .name("name")
                .email(email)
                .profileImg("profileImg")
                .build();
    }
}