package com.wootecam.festivals.domain.festival.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.member.entity.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FestivalValidatorTest {

    @Nested
    @DisplayName("validateFestival 메소드는")
    class Describe_validateFestival {

        @Test
        @DisplayName("유효한 입력값으로 호출하면 예외를 발생시키지 않는다")
        void it_does_not_throw_exception_with_valid_input() {
            Member admin = Member.builder()
                    .name("admin")
                    .email("admin@test.com")
                    .profileImg("profile.jps")
                    .build();
            String title = "Valid Title";
            String description = "Valid Description";
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = LocalDateTime.now().plusDays(2);

            assertThatCode(() -> FestivalValidator.validateFestival(admin, title, description, startTime, endTime))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("admin이 null이면 NullPointerException을 발생시킨다")
        void it_throws_NullPointerException_when_admin_is_null() {
            assertThatThrownBy(() -> FestivalValidator.validateFestival(null, "title", "description",
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("관리자는 null일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("validateTitle 메소드는")
    class Describe_validateTitle {

        @Test
        @DisplayName("유효한 제목으로 호출하면 예외를 발생시키지 않는다")
        void it_does_not_throw_exception_with_valid_title() {
            assertThatCode(() -> FestivalValidator.validateTitle("Valid Title"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 제목으로 호출하면 NullPointerException을 발생시킨다")
        void it_throws_NullPointerException_when_title_is_null() {
            assertThatThrownBy(() -> FestivalValidator.validateTitle(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("제목은 null일 수 없습니다.");
        }

        @Test
        @DisplayName("빈 제목으로 호출하면 IllegalArgumentException을 발생시킨다")
        void it_throws_IllegalArgumentException_when_title_is_empty() {
            assertThatThrownBy(() -> FestivalValidator.validateTitle(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("제목은 비어있을 수 없습니다.");
        }

        @Test
        @DisplayName("100자를 초과하는 제목으로 호출하면 IllegalArgumentException을 발생시킨다")
        void it_throws_IllegalArgumentException_when_title_exceeds_max_length() {
            String longTitle = "a".repeat(101);
            assertThatThrownBy(() -> FestivalValidator.validateTitle(longTitle))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("제목의 길이는 100를 초과해서는 안됩니다.");
        }
    }

    @Nested
    @DisplayName("validateDescription 메소드는")
    class Describe_validateDescription {

        @Test
        @DisplayName("유효한 설명으로 호출하면 예외를 발생시키지 않는다")
        void it_does_not_throw_exception_with_valid_description() {
            assertThatCode(() -> FestivalValidator.validateDescription("Valid Description"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 설명으로 호출하면 NullPointerException을 발생시킨다")
        void it_throws_NullPointerException_when_description_is_null() {
            assertThatThrownBy(() -> FestivalValidator.validateDescription(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("설명은 null일 수 없습니다.");
        }

        @Test
        @DisplayName("빈 설명으로 호출하면 IllegalArgumentException을 발생시킨다")
        void it_throws_IllegalArgumentException_when_description_is_empty() {
            assertThatThrownBy(() -> FestivalValidator.validateDescription(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("설명은 비어있을 수 없습니다.");
        }

        @Test
        @DisplayName("2000자를 초과하는 설명으로 호출하면 IllegalArgumentException을 발생시킨다")
        void it_throws_IllegalArgumentException_when_description_exceeds_max_length() {
            String longDescription = "a".repeat(2001);
            assertThatThrownBy(() -> FestivalValidator.validateDescription(longDescription))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("설명의 길이는 2000를 초과해서는 안됩니다.");
        }
    }

    @Nested
    @DisplayName("validateTimeRange 메소드는")
    class Describe_validateTimeRange {

        @Test
        @DisplayName("유효한 시간 범위로 호출하면 예외를 발생시키지 않는다")
        void it_does_not_throw_exception_with_valid_time_range() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = LocalDateTime.now().plusDays(2);
            assertThatCode(() -> FestivalValidator.validateTimeRange(startTime, endTime))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("시작 시간이 현재보다 과거이면 IllegalArgumentException을 발생시킨다")
        void it_throws_IllegalArgumentException_when_start_time_is_in_the_past() {
            LocalDateTime startTime = LocalDateTime.now().minusDays(1);
            LocalDateTime endTime = LocalDateTime.now().plusDays(1);
            assertThatThrownBy(() -> FestivalValidator.validateTimeRange(startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("시작 시간은 현재보다 미래여야 합니다.");
        }

        @Test
        @DisplayName("종료 시간이 현재보다 과거이면 IllegalArgumentException을 발생시킨다")
        void it_throws_IllegalArgumentException_when_end_time_is_in_the_past() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = LocalDateTime.now().minusDays(1);
            assertThatThrownBy(() -> FestivalValidator.validateTimeRange(startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("종료 시간은 현재보다 미래여야 합니다.");
        }

        @Test
        @DisplayName("시작 시간이 종료 시간보다 늦으면 IllegalArgumentException을 발생시킨다")
        void it_throws_IllegalArgumentException_when_start_time_is_after_end_time() {
            LocalDateTime startTime = LocalDateTime.now().plusDays(2);
            LocalDateTime endTime = LocalDateTime.now().plusDays(1);
            assertThatThrownBy(() -> FestivalValidator.validateTimeRange(startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("시작 시간은 종료 시간보다 앞서야 합니다.");
        }
    }

    @Nested
    @DisplayName("isValidStatusTransition 메소드는")
    class Describe_isValidStatusTransition {

        @ParameterizedTest
        @CsvSource({
                "UPCOMING, ONGOING, true",
                "UPCOMING, COMPLETED, true",
                "ONGOING, COMPLETED, true",
                "COMPLETED, UPCOMING, false",
                "COMPLETED, ONGOING, false",
                "ONGOING, UPCOMING, false"
        })
        @DisplayName("현재 상태와 새로운 상태에 따라 적절한 결과를 반환한다")
        void it_returns_correct_result_based_on_current_and_new_status(FestivalProgressStatus currentStatus,
                                                                       FestivalProgressStatus newStatus,
                                                                       boolean expected) {
            boolean result = FestivalValidator.isValidStatusTransition(currentStatus, newStatus);
            assertThat(result).isEqualTo(expected);
        }
    }
}