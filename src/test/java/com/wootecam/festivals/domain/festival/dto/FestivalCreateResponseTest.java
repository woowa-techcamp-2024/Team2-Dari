package com.wootecam.festivals.domain.festival.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
import com.wootecam.festivals.global.exception.type.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FestivalCreateResponseDto 테스트")
class FestivalCreateResponseTest {

    @Nested
    @DisplayName("from 메서드는")
    class Describe_from {

        @Test
        @DisplayName("유효한 Festival 객체가 주어지면 FestivalCreateResponseDto를 반환한다")
        void it_returns_festivalCreateResponseDto_when_given_valid_festival() {
            // Given
            Long expectedId = 1L;
            Festival festival = FestivalStub.createValidFestival(expectedId);

            // When
            FestivalCreateResponse responseDto = FestivalCreateResponse.from(festival);

            // Then
            assertThat(responseDto.festivalId()).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("null Festival 객체가 주어지면 ApiException을 던진다")
        void it_throws_apiException_when_given_null_festival() {
            // When & Then
            assertThatThrownBy(() -> FestivalCreateResponse.from(null))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", FestivalErrorCode.InvalidFestivalDataException)
                    .hasMessage("Festival 객체가 null입니다.");
        }

        @Test
        @DisplayName("ID가 null인 Festival 객체가 주어지면 ApiException을 던진다")
        void it_throws_apiException_when_given_festival_with_null_id() {
            // Given
            Festival festivalWithNullId = FestivalStub.createFestivalWithNullId();

            // When & Then
            assertThatThrownBy(() -> FestivalCreateResponse.from(festivalWithNullId))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", FestivalErrorCode.InvalidFestivalDataException)
                    .hasMessage("Festival Id가 null입니다.");
        }
    }
}