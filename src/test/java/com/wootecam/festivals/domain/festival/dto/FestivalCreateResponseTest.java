package com.wootecam.festivals.domain.festival.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
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
    }
}