package com.wootecam.festivals.domain.auth.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("LoginRequest 테스트")
class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private record TestCase(String email, int expectedViolationCount, String... expectedMessages) {
    }

    @Nested
    @DisplayName("이메일 테스트")
    class EmailTests {
        @Nested
        @DisplayName("유효한 이메일 테스트")
        class ValidEmailTests {

            static Stream<Arguments> provideValidEmails() {
                return Stream.of(
                        Arguments.of(new TestCase("test@example.com", 0)),
                        Arguments.of(new TestCase("user.name+tag@example.com", 0)),
                        Arguments.of(new TestCase("a".repeat(64) + "@example.com", 0))
                );
            }

            @ParameterizedTest
            @MethodSource("provideValidEmails")
            @DisplayName("유효한 이메일 주소는 검증을 통과해야 한다")
            void testValidEmails(TestCase testCase) {
                LoginRequest request = new LoginRequest(testCase.email);
                var violations = validator.validate(request);

                assertTrue(violations.isEmpty(), "Valid email should not have any violations");
            }
        }

        @Nested
        @DisplayName("유효하지 않은 이메일 테스트")
        class InvalidEmailTests {

            static Stream<Arguments> provideInvalidEmails() {
                return Stream.of(
                        Arguments.of(new TestCase("testexample.com", 1, "이메일 형식이 올바르지 않습니다.")),
                        Arguments.of(new TestCase("", 2, "이메일은 1자 이상 100자 이하여야 합니다.", "이메일은 필수입니다.")),
                        Arguments.of(new TestCase(null, 1, "이메일은 필수입니다.")),
                        Arguments.of(new TestCase("a".repeat(65) + "@example.com", 1, "이메일 형식이 올바르지 않습니다.")),
                        Arguments.of(new TestCase("test@example.com" + "a".repeat(101), 2, "이메일은 1자 이상 100자 이하여야 합니다.",
                                "이메일 형식이 올바르지 않습니다."))
                );
            }

            @ParameterizedTest
            @MethodSource("provideInvalidEmails")
            @DisplayName("유효하지 않은 이메일 주소는 검증에 실패해야 한다")
            void testInvalidEmails(TestCase testCase) {
                LoginRequest request = new LoginRequest(testCase.email);
                var violations = validator.validate(request);

                assertEquals(testCase.expectedViolationCount, violations.size(),
                        "Violation count should match expected count");

                Set<String> actualMessages = violations.stream()
                        .map(v -> v.getMessage())
                        .collect(java.util.stream.Collectors.toSet());

                for (String expectedMessage : testCase.expectedMessages) {
                    assertTrue(actualMessages.contains(expectedMessage),
                            "Expected violation message not found: " + expectedMessage);
                }
            }

        }
    }
}