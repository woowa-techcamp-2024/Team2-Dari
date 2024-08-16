package com.wootecam.festivals.domain.member.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wootecam.festivals.domain.member.entity.Member;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("MemberCreateRequest 테스트")
class MemberCreateRequestTest {

    private Validator validator;
    private static final int MAX_FRONT_EMAIL_LENGTH = 64; // 이메일 @ 앞부분 최대 크기

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private record TestCase(String name, String email, String profileImg, int expectedViolationCount, String... expectedMessages) {
    }

    @Nested
    @DisplayName("이름 테스트")
    class NameTests {
        @Nested
        @DisplayName("유효한 이름 테스트")
        class ValidNameTests {

            static Stream<Arguments> provideValidNames() {
                return Stream.of(
                        Arguments.of(new TestCase("John", "john@example.com", "profile.jpg", 0)),
                        Arguments.of(new TestCase("A".repeat(20), "john@example.com", "profile.jpg", 0))
                );
            }

            @ParameterizedTest
            @MethodSource("provideValidNames")
            @DisplayName("유효한 이름은 검증을 통과해야 한다")
            void testValidNames(TestCase testCase) {
                MemberCreateRequest request = new MemberCreateRequest(testCase.name, testCase.email, testCase.profileImg);
                var violations = validator.validate(request);

                assertTrue(violations.isEmpty(), "Valid name should not have any violations");
            }
        }

        @Nested
        @DisplayName("유효하지 않은 이름 테스트")
        class InvalidNameTests {

            static Stream<Arguments> provideInvalidNames() {
                return Stream.of(
                        Arguments.of(new TestCase("", "john@example.com", "profile.jpg", 2, "이름은 필수입니다.", "이름은 1자 이상 20자 이하여야 합니다.")),
                        Arguments.of(new TestCase(null, "john@example.com", "profile.jpg", 1, "이름은 필수입니다.")),
                        Arguments.of(new TestCase("A".repeat(21), "john@example.com", "profile.jpg", 1, "이름은 1자 이상 20자 이하여야 합니다."))
                );
            }

            @ParameterizedTest
            @MethodSource("provideInvalidNames")
            @DisplayName("유효하지 않은 이름은 검증에 실패해야 한다")
            void testInvalidNames(TestCase testCase) {
                MemberCreateRequest request = new MemberCreateRequest(testCase.name, testCase.email, testCase.profileImg);
                var violations = validator.validate(request);

                assertEquals(testCase.expectedViolationCount, violations.size(),
                        "Violation count should match expected count");

                Set<String> actualMessages = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toSet());

                for (String expectedMessage : testCase.expectedMessages) {
                    assertTrue(actualMessages.contains(expectedMessage),
                            "Expected violation message not found: " + expectedMessage);
                }
            }

            @Test
            @DisplayName("Null 이름은 검증에 실패해야 한다")
            void testNullName() {
                MemberCreateRequest request = new MemberCreateRequest(null, "john@example.com", "profile.jpg");
                var violations = validator.validate(request);

                assertEquals(1, violations.size(), "Null name should have one violation");
                assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("이름은 필수입니다.")),
                        "Violation message for null name not found");
            }
        }
    }

    @Nested
    @DisplayName("이메일 테스트")
    class EmailTests {
        @Nested
        @DisplayName("유효한 이메일 테스트")
        class ValidEmailTests {

            static Stream<Arguments> provideValidEmails() {
                return Stream.of(
                        Arguments.of(new TestCase("John", "test@example.com", "profile.jpg", 0)),
                        Arguments.of(new TestCase("John", "user.name+tag@example.com", "profile.jpg", 0)),
                        Arguments.of(new TestCase("John", "a".repeat(MAX_FRONT_EMAIL_LENGTH) + "@example.com", "profile.jpg", 0))
                );
            }

            @ParameterizedTest
            @MethodSource("provideValidEmails")
            @DisplayName("유효한 이메일 주소는 검증을 통과해야 한다")
            void testValidEmails(TestCase testCase) {
                MemberCreateRequest request = new MemberCreateRequest(testCase.name, testCase.email, testCase.profileImg);
                var violations = validator.validate(request);

                assertTrue(violations.isEmpty(), "Valid email should not have any violations");
            }
        }

        @Nested
        @DisplayName("유효하지 않은 이메일 테스트")
        class InvalidEmailTests {

            static Stream<Arguments> provideInvalidEmails() {
                return Stream.of(
                        Arguments.of(new TestCase("John", "testexample.com", "profile.jpg", 1, "이메일 형식이 올바르지 않습니다.")),
                        Arguments.of(new TestCase("John", "", "profile.jpg", 2, "이메일은 1자 이상 100자 이하여야 합니다.", "이메일은 필수입니다.")),
                        Arguments.of(new TestCase("John", null, "profile.jpg", 1, "이메일은 필수입니다.")),
                        Arguments.of(new TestCase("John", "a".repeat(101) + "@example.com", "profile.jpg", 2, "이메일 형식이 올바르지 않습니다.", "이메일은 1자 이상 100자 이하여야 합니다."))
                );
            }

            @ParameterizedTest
            @MethodSource("provideInvalidEmails")
            @DisplayName("유효하지 않은 이메일 주소는 검증에 실패해야 한다")
            void testInvalidEmails(TestCase testCase) {
                MemberCreateRequest request = new MemberCreateRequest(testCase.name, testCase.email, testCase.profileImg);
                var violations = validator.validate(request);

                assertEquals(testCase.expectedViolationCount, violations.size(),
                        "Violation count should match expected count");

                Set<String> actualMessages = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toSet());

                for (String expectedMessage : testCase.expectedMessages) {
                    assertTrue(actualMessages.contains(expectedMessage),
                            "Expected violation message not found: " + expectedMessage);
                }
            }

            @Test
            @DisplayName("Null 이메일은 검증에 실패해야 한다")
            void testNullEmail() {
                MemberCreateRequest request = new MemberCreateRequest("John", null, "profile.jpg");
                var violations = validator.validate(request);

                assertEquals(1, violations.size(), "Null email should have one violation");
                assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("이메일은 필수입니다.")),
                        "Violation message for null email not found");
            }
        }
    }

    @Nested
    @DisplayName("프로필 이미지 테스트")
    class ProfileImgTests {
        @Test
        @DisplayName("Null 프로필 이미지는 허용되어야 한다")
        void testNullProfileImg() {
            MemberCreateRequest request = new MemberCreateRequest("John", "john@example.com", null);
            var violations = validator.validate(request);

            assertTrue(violations.isEmpty(), "Null profile image should not have any violations");
        }
    }

    @Nested
    @DisplayName("toEntity 메소드 테스트")
    class toEntityTests {
        @Test
        @DisplayName("toEntity 메소드는 올바른 Member 엔티티를 생성해야 한다")
        void toEntityMethodShouldCreateCorrectMemberEntity() {
            MemberCreateRequest request = new MemberCreateRequest("John Doe", "john@example.com", "profile.jpg");
            Member member = request.toEntity();

            assertEquals("John Doe", member.getName());
            assertEquals("john@example.com", member.getEmail());
            assertEquals("profile.jpg", member.getProfileImg());
        }

        @Test
        @DisplayName("toEntity 메소드는 Null 프로필 이미지를 허용해야 한다")
        void toEntityMethodShouldAllowNullProfileImg() {
            MemberCreateRequest request = new MemberCreateRequest("John Doe", "john@example.com", null);
            Member member = request.toEntity();

            assertEquals("John Doe", member.getName());
            assertEquals("john@example.com", member.getEmail());
            assertNull(null, member.getProfileImg());
        }
    }
}