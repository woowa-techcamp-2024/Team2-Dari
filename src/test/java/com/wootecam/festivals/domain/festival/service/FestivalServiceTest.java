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
import com.wootecam.festivals.domain.organization.entity.Organization;
import com.wootecam.festivals.domain.organization.repository.OrganizationRepository;
import com.wootecam.festivals.global.exception.GlobalErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("FestivalService 통합 테스트")
class FestivalServiceTest extends SpringBootTestConfig {

    @Autowired
    private FestivalService festivalService;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization testOrganization;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(festivalRepository);
        TestDBCleaner.clear(organizationRepository);
        testOrganization = organizationRepository.save(
                Organization.builder()
                        .name("Test Organization")
                        .detail("Test Detail")
                        .profileImg("Test profileImg")
                        .build());
    }

    @Nested
    @DisplayName("createFestival 메서드는")
    class CreateFestival {

        @Test
        @DisplayName("유효한 정보로 축제를 생성할 수 있다")
        void createValidFestival() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            FestivalCreateRequest requestDto = new FestivalCreateRequest(
                    testOrganization.getId(),
                    "테스트 축제",
                    "축제 설명",
                    now.plusDays(1),
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
                        assertThat(festival.getOrganization().getId()).isEqualTo(testOrganization.getId());
                        assertThat(festival.getTitle()).isEqualTo("테스트 축제");
                        assertThat(festival.getDescription()).isEqualTo("축제 설명");
                        assertThat(festival.getStartTime()).isCloseTo(now.plusDays(1), within(1, ChronoUnit.SECONDS));
                        assertThat(festival.getEndTime()).isCloseTo(now.plusDays(7), within(1, ChronoUnit.SECONDS));
                    });
        }

        @Test
        @DisplayName("존재하지 않는 조직 ID로 축제 생성 시 예외를 던진다")
        void createFestivalWithNonExistentOrganization() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            FestivalCreateRequest requestDto = new FestivalCreateRequest(
                    9999L,
                    "테스트 축제",
                    "축제 설명",
                    now.plusDays(1),
                    now.plusDays(7)
            );

            // When & Then
            assertThatThrownBy(() -> festivalService.createFestival(requestDto))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.INVALID_REQUEST_PARAMETER)
                    .hasMessageContaining("유효하지 않는 조직입니다.");
        }

        @Test
        @DisplayName("과거의 시작 시간으로 축제 생성 시 예외를 던진다")
        void createFestivalWithPastStartTime() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            FestivalCreateRequest requestDto = new FestivalCreateRequest(
                    testOrganization.getId(),
                    "테스트 축제",
                    "축제 설명",
                    now.minusDays(1),
                    now.plusDays(7)
            );

            // When & Then
            assertThatThrownBy(() -> festivalService.createFestival(requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작 시간은 현재보다 미래여야 합니다.");
        }

        @Test
        @DisplayName("종료 시간이 시작 시간보다 빠른 경우 예외를 던진다")
        void createFestivalWithEndTimeBeforeStartTime() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            FestivalCreateRequest requestDto = new FestivalCreateRequest(
                    testOrganization.getId(),
                    "테스트 축제",
                    "축제 설명",
                    now.plusDays(7),
                    now.plusDays(1)
            );

            // When & Then
            assertThatThrownBy(() -> festivalService.createFestival(requestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작 시간은 종료 시간보다 앞어야만 합니다.");
        }
    }

    @Nested
    @DisplayName("getFestivalDetail 메서드는")
    class GetFestivalDetail {

        @Test
        @DisplayName("존재하는 축제 ID로 상세 정보를 조회할 수 있다")
        void getFestivalDetailSuccess() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Festival festival = Festival.builder()
                    .organization(testOrganization)
                    .title("테스트 축제")
                    .description("축제 설명")
                    .startTime(now.plusDays(1))
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
                        assertThat(detail.startTime()).isCloseTo(now.plusDays(1), within(1, ChronoUnit.SECONDS));
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

        @Test
        @DisplayName("삭제된 축제 조회 시 예외를 던진다")
        void getFestivalDetailDeleted() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            Festival festival = Festival.builder()
                    .organization(testOrganization)
                    .title("삭제된 축제")
                    .description("이 축제는 삭제되었습니다")
                    .startTime(now.plusDays(1))
                    .endTime(now.plusDays(7))
                    .build();
            festival.delete();
            Festival savedFestival = festivalRepository.save(festival);

            // When & Then
            assertThatThrownBy(() -> festivalService.getFestivalDetail(savedFestival.getId()))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", FestivalErrorCode.FestivalNotFoundException);
        }
    }
}