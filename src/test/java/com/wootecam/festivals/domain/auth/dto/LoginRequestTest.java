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
import org.junit.jupiter.api.Test;

class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidEmail() {
        LoginRequest request = new LoginRequest("test@example.com");
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
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
    void testNullEmail() {
        LoginRequest request = new LoginRequest(null);
        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("이메일은 필수입니다.", violations.iterator().next().getMessage());
    }

    @Test
    void testTooLongEmail() {
        String longEmail = "a".repeat(101) + "@example.com";
        LoginRequest request = new LoginRequest(longEmail);
        var violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("이메일은 1자 이상 100자 이하여야 합니다.", violations.iterator().next().getMessage());
    }
}