package com.wootecam.festivals.domain.festival.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalDetailResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FestivalService의 ")
class FestivalServiceTest {

    @Autowired
    FestivalService festivalService;

    @Autowired
    FestivalRepository festivalRepository;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(festivalRepository);
    }

    @Nested
    @DisplayName("축제 생성 메서드는 ")
    class CreateFestival {

        @Test
        @DisplayName("유효한 정보로 축제를 생성할 수 있다")
        void createValidFestival() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            FestivalCreateRequest requestDto = new FestivalCreateRequest(
                    1L, // organizationId
                    "테스트 축제",
                    "축제 설명",
                    now,
                    now.plusDays(7)
            );

            // When
            FestivalCreateResponse responseDto = festivalService.createFestival(requestDto);

            // Then
            assertThat(responseDto).isNotNull();

            Festival savedFestival = festivalRepository.findById(responseDto.festivalId())
                    .orElseThrow(() -> new AssertionError("저장된 축제를 찾을 수 없습니다."));

            assertThat(savedFestival)
                    .satisfies(festival -> {
                        assertThat(festival.getOrganizationId()).isEqualTo(1L);
                        assertThat(festival.getTitle()).isEqualTo("테스트 축제");
                        assertThat(festival.getDescription()).isEqualTo("축제 설명");
                        assertThat(festival.getStartTime()).isCloseTo(now, within(1, ChronoUnit.SECONDS));
                        assertThat(festival.getEndTime()).isCloseTo(now.plusDays(7), within(1, ChronoUnit.SECONDS));
                    });
        }
    }

    @Nested
    @DisplayName("축제 상세 조회 메서드는 ")
    class GetFestivalDetail {

        @Test
        @DisplayName("존재하는 축제 ID로 상세 정보를 조회할 수 있다")
        void getFestivalDetailSuccess() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Festival festival = Festival.builder()
                    .organizationId(1L)
                    .title("테스트 축제")
                    .description("축제 설명")
                    .startTime(now)
                    .endTime(now.plusDays(7))
                    .build();
            Festival savedFestival = festivalRepository.save(festival);

            // When
            FestivalDetailResponse response = festivalService.getFestivalDetail(savedFestival.getId());

            // Then
            assertThat(response).isNotNull()
                    .satisfies(detail -> {
                        assertThat(detail.festivalId()).isEqualTo(savedFestival.getId());
                        assertThat(detail.title()).isEqualTo("테스트 축제");
                        assertThat(detail.description()).isEqualTo("축제 설명");
                        assertThat(detail.startTime()).isCloseTo(now, within(1, ChronoUnit.SECONDS));
                        assertThat(detail.endTime()).isCloseTo(now.plusDays(7), within(1, ChronoUnit.SECONDS));
                    });
        }

        @Test
        @DisplayName("존재하지 않는 축제 ID로 조회 시 예외를 던진다")
        void getFestivalDetailNotFound() {
            // Given
            Long nonExistentId = 9999L;

            // When & Then
            assertThatThrownBy(() -> festivalService.getFestivalDetail(nonExistentId))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", FestivalErrorCode.FestivalNotFoundException);
        }

        @Test
        @DisplayName("null ID로 조회 시 예외를 던진다")
        void getFestivalDetailNullId() {
            // When & Then
            assertThatThrownBy(() -> festivalService.getFestivalDetail(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Festival ID는 null일 수 없습니다.");
        }
    }
}