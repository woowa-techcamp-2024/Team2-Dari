package com.wootecam.festivals.global.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.auth.exception.AuthErrorCode;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.utils.AuthenticationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor(memberRepository);
    }

    @Nested
    @DisplayName("preHandle 메소드")
    class PreHandleMethod {

        @Test
        @DisplayName("인증 정보가 없을 때 예외를 던진다")
        void throwsExceptionWhenNoAuthentication() {
            try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
                authUtils.when(AuthenticationUtils::getAuthentication).thenReturn(null);

                assertThrows(ApiException.class, () -> authInterceptor.preHandle(request, response, null));
            }
        }

        @Test
        @DisplayName("유효한 인증 정보로 true를 반환한다")
        void returnsTrueWithValidAuthentication() {
            Authentication auth = new Authentication(1L, "Test User", "test@example.com");

            Member member = Member.builder()
                    .email("test@example.com")
                    .name("Test User")
                    .build();

            try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
                authUtils.when(AuthenticationUtils::getAuthentication).thenReturn(auth);
                when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

                assertTrue(authInterceptor.preHandle(request, response, null));
            }
        }

        @Test
        @DisplayName("회원 정보가 일치하지 않을 때 예외를 던진다")
        void throwsExceptionWhenMemberInfoMismatch() {
            Authentication auth = new Authentication(1L, "test@example.com", "Test User");
            Member member = Member.builder()
                    .email("wrong_email@example.com")
                    .name("Wrong User")
                    .build();

            try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
                authUtils.when(AuthenticationUtils::getAuthentication).thenReturn(auth);
                when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

                ApiException exception = assertThrows(ApiException.class,
                        () -> authInterceptor.preHandle(request, response, null));
                assertEquals(AuthErrorCode.UNAUTHORIZED, exception.getErrorCode());
            }
        }

        @Test
        @DisplayName("회원이 존재하지 않을 때 예외를 던진다")
        void throwsExceptionWhenMemberNotFound() {
            Authentication auth = new Authentication(1L, "test@example.com", "Test User");

            try (MockedStatic<AuthenticationUtils> authUtils = mockStatic(AuthenticationUtils.class)) {
                authUtils.when(AuthenticationUtils::getAuthentication).thenReturn(auth);
                when(memberRepository.findById(1L)).thenReturn(Optional.empty());

                ApiException exception = assertThrows(ApiException.class,
                        () -> authInterceptor.preHandle(request, response, null));
                assertEquals(AuthErrorCode.UNAUTHORIZED, exception.getErrorCode());
            }
        }
    }
}