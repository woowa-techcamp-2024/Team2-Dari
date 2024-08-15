package com.wootecam.festivals.domain.auth.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("이메일 형식이 올바르면 유효성 검사를 통과한다")
    void testValidEmail() {
        LoginRequest request = new LoginRequest("test@example.com");
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 유효성 검사를 통과한다")
    void testInvalidEmail() {
        LoginRequest request = new LoginRequest("testexample.com");
        var violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("이메일 형식이 올바르지 않습니다.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("이메일이 빈 문자열이면 유효성 검사에 실패한다")
    void testBlankEmail() {
        LoginRequest request = new LoginRequest("");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());

        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violationMessages.contains("이메일은 1자 이상 100자 이하여야 합니다."));
        assertTrue(violationMessages.contains("이메일은 필수입니다."));
    }

    @Test
    @DisplayName("이메일이 null이면 유효성 검사에 실패한다")
    void testNullEmail() {
        LoginRequest request = new LoginRequest(null);
        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("이메일은 필수입니다.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("이메일의 @ 앞부분이 64자를 초과하면 유효성 검사에 실패한다")
    void testTooLonFrontPartEmail() {
        String longFrontPartEmail = "a".repeat(64 + 1) + "@example.com";
        LoginRequest request = new LoginRequest(longFrontPartEmail);

        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("이메일 형식이 올바르지 않습니다.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("이메일이 100자를 초과하면 유효성 검사에 실패한다")
    void testTooLongEmail() {
        String longEmail = "test" + "@example.com" + "a".repeat(101);
        LoginRequest request = new LoginRequest(longEmail);
        var violations = validator.validate(request);

        assertEquals(2, violations.size());
        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(violationMessages.contains("이메일은 1자 이상 100자 이하여야 합니다."));
        assertTrue(violationMessages.contains("이메일 형식이 올바르지 않습니다."));
    }
}