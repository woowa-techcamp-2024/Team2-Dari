package com.wootecam.festivals.domain.festival.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FestivalTest {

    private static final Long VALID_ORGANIZATION_ID = 1L;
    private static final String VALID_TITLE = "유효한 제목";
    private static final String VALID_DESCRIPTION = "유효한 설명";
    private static final LocalDateTime VALID_START_TIME = LocalDateTime.now().plusDays(1);
    private static final LocalDateTime VALID_END_TIME = VALID_START_TIME.plusHours(2);

    @Nested
    @DisplayName("Festival 생성 테스트는")
    class CreateFestival {

        @Test
        @DisplayName("유효한 데이터로 Festival 생성 성공한다")
        void createFestivalWithValidData() {
            // Given & When
            Festival festival = Festival.builder()
                    .organizationId(VALID_ORGANIZATION_ID)
                    .title(VALID_TITLE)
                    .description(VALID_DESCRIPTION)
                    .startTime(VALID_START_TIME)
                    .endTime(VALID_END_TIME)
                    .build();

            // Then
            assertThat(festival).isNotNull();
            assertThat(festival.getOrganizationId()).isEqualTo(VALID_ORGANIZATION_ID);
            assertThat(festival.getTitle()).isEqualTo(VALID_TITLE);
            assertThat(festival.getDescription()).isEqualTo(VALID_DESCRIPTION);
            assertThat(festival.getStartTime()).isEqualTo(VALID_START_TIME);
            assertThat(festival.getEndTime()).isEqualTo(VALID_END_TIME);
        }

        @Test
        @DisplayName("null 값으로 Festival 생성 시 예외가 발생한다")
        void createFestivalWithNullValuesShouldThrowException() {
            // Given & When & Then
            assertThatThrownBy(() -> Festival.builder().build())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("제목 유효성 검사")
    class ValidateTitle {

        @Test
        @DisplayName("빈 제목으로 Festival 생성 시 예외 발생한다")
        void createFestivalWithEmptyTitleShouldThrowException() {
            // Given & When & Then
            assertThatThrownBy(() ->
                    Festival.builder()
                            .organizationId(VALID_ORGANIZATION_ID)
                            .title("")
                            .description(VALID_DESCRIPTION)
                            .startTime(VALID_START_TIME)
                            .endTime(VALID_END_TIME)
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("제목은 비어있을 수 없습니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {101, 150, 200})
        @DisplayName("제목이 최대 길이를 초과할 경우 예외 발생한다")
        void createFestivalWithTooLongTitleShouldThrowException(int titleLength) {
            // Given
            String tooLongTitle = "a".repeat(titleLength);

            // When & Then
            assertThatThrownBy(() ->
                    Festival.builder()
                            .organizationId(VALID_ORGANIZATION_ID)
                            .title(tooLongTitle)
                            .description(VALID_DESCRIPTION)
                            .startTime(VALID_START_TIME)
                            .endTime(VALID_END_TIME)
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("제목의 길이는 100를 초과해서는 안됩니다.");
        }
    }

    @Nested
    @DisplayName("설명 유효성 검사")
    class ValidateDescription {

        @Test
        @DisplayName("빈 설명으로 Festival 생성 시 예외 발생한다")
        void createFestivalWithEmptyDescriptionShouldThrowException() {
            // Given & When & Then
            assertThatThrownBy(() ->
                    Festival.builder()
                            .organizationId(VALID_ORGANIZATION_ID)
                            .title(VALID_TITLE)
                            .description("")
                            .startTime(VALID_START_TIME)
                            .endTime(VALID_END_TIME)
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Description은 비어있을 수 없습니다.");
        }

        @ParameterizedTest
        @ValueSource(ints = {2001, 2500, 3000})
        @DisplayName("설명이 최대 길이를 초과할 경우 예외 발생한다")
        void createFestivalWithTooLongDescriptionShouldThrowException(int descriptionLength) {
            // Given
            String tooLongDescription = "a".repeat(descriptionLength);

            // When & Then
            assertThatThrownBy(() ->
                    Festival.builder()
                            .organizationId(VALID_ORGANIZATION_ID)
                            .title(VALID_TITLE)
                            .description(tooLongDescription)
                            .startTime(VALID_START_TIME)
                            .endTime(VALID_END_TIME)
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Description의 길이는 2000를 초과해서는 안됩니다.");
        }
    }

    @Nested
    @DisplayName("시간 범위 유효성 검사")
    class ValidateTimeRange {

        @Test
        @DisplayName("시작 시간이 종료 시간보다 늦을 경우 예외 발생")
        void createFestivalWithStartTimeAfterEndTimeShouldThrowException() {
            // Given
            LocalDateTime invalidStartTime = VALID_END_TIME.plusHours(1);

            // When & Then
            assertThatThrownBy(() ->
                    Festival.builder()
                            .organizationId(VALID_ORGANIZATION_ID)
                            .title(VALID_TITLE)
                            .description(VALID_DESCRIPTION)
                            .startTime(invalidStartTime)
                            .endTime(VALID_END_TIME)
                            .build()
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("시작 시간은 종료 시간보다 앞어야만 합니다.");
        }
    }
}