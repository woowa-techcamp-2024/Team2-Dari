package com.wootecam.festivals.domain.festival.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequestDto;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
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
            FestivalCreateRequestDto requestDto = new FestivalCreateRequestDto(
                    1L, // organizationId
                    "테스트 축제",
                    "축제 설명",
                    now,
                    now.plusDays(7)
            );

            // When
            Long festivalId = festivalService.createFestival(requestDto);

            // Then
            assertThat(festivalId).isNotNull();

            Festival savedFestival = festivalRepository.findById(festivalId)
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
}