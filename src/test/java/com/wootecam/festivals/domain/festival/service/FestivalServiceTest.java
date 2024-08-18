package com.wootecam.festivals.domain.festival.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalIdResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalResponse;
import com.wootecam.festivals.domain.festival.dto.KeySetPageResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.exception.GlobalErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;
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
    private MemberRepository memberRepository;

    private Member admin;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(festivalRepository);
        TestDBCleaner.clear(memberRepository);
        admin = memberRepository.save(
                Member.builder()
                        .name("Test Organization")
                        .email("Test Detail")
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
                    "테스트 축제",
                    "축제 설명",
                    now.plusDays(1),
                    now.plusDays(7)
            );

            // When
            FestivalIdResponse responseDto = festivalService.createFestival(requestDto, admin.getId());

            // Then
            assertThat(responseDto).isNotNull();

            Festival savedFestival = festivalRepository.findById(responseDto.festivalId())
                    .orElseThrow(() -> new AssertionError("저장된 축제를 찾을 수 없습니다."));

            assertThat(savedFestival)
                    .satisfies(festival -> {
                        assertThat(festival.getAdmin().getId()).isEqualTo(admin.getId());
                        assertThat(festival.getTitle()).isEqualTo("테스트 축제");
                        assertThat(festival.getDescription()).isEqualTo("축제 설명");
                        assertThat(festival.getStartTime()).isCloseTo(now.plusDays(1), within(59, ChronoUnit.SECONDS));
                        assertThat(festival.getEndTime()).isCloseTo(now.plusDays(7), within(59, ChronoUnit.SECONDS));
                    });
        }

        @Test
        @DisplayName("존재하지 않는 조직 ID로 축제 생성 시 예외를 던진다")
        void createFestivalWithNonExistentOrganization() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            FestivalCreateRequest requestDto = new FestivalCreateRequest(
                    "테스트 축제",
                    "축제 설명",
                    now.plusDays(1),
                    now.plusDays(7)
            );

            // When & Then
            assertThatThrownBy(() -> festivalService.createFestival(requestDto, 9999L))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", GlobalErrorCode.INVALID_REQUEST_PARAMETER)
                    .hasMessageContaining("유효하지 않는 멤버입니다.");
        }

        @Test
        @DisplayName("과거의 시작 시간으로 축제 생성 시 예외를 던진다")
        void createFestivalWithPastStartTime() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            FestivalCreateRequest requestDto = new FestivalCreateRequest(
                    "테스트 축제",
                    "축제 설명",
                    now.minusDays(1),
                    now.plusDays(7)
            );

            // When & Then
            assertThatThrownBy(() -> festivalService.createFestival(requestDto, admin.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작 시간은 현재보다 미래여야 합니다.");
        }

        @Test
        @DisplayName("종료 시간이 시작 시간보다 빠른 경우 예외를 던진다")
        void createFestivalWithEndTimeBeforeStartTime() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            FestivalCreateRequest requestDto = new FestivalCreateRequest(
                    "테스트 축제",
                    "축제 설명",
                    now.plusDays(7),
                    now.plusDays(1)
            );

            // When & Then
            assertThatThrownBy(() -> festivalService.createFestival(requestDto, admin.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작 시간은 종료 시간보다 앞서야 합니다.");
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
                    .admin(admin)
                    .title("테스트 축제")
                    .description("축제 설명")
                    .startTime(now.plusDays(1))
                    .endTime(now.plusDays(7))
                    .build();
            Festival savedFestival = festivalRepository.save(festival);

            // When
            FestivalResponse response = festivalService.getFestivalDetail(savedFestival.getId());

            // Then
            assertThat(response).isNotNull()
                    .satisfies(detail -> {
                        assertThat(detail.festivalId()).isEqualTo(savedFestival.getId());
                        assertThat(detail.title()).isEqualTo("테스트 축제");
                        assertThat(detail.description()).isEqualTo("축제 설명");
                        assertThat(detail.startTime()).isCloseTo(now.plusDays(1), within(59, ChronoUnit.SECONDS));
                        assertThat(detail.endTime()).isCloseTo(now.plusDays(7), within(59, ChronoUnit.SECONDS));
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
                    .hasFieldOrPropertyWithValue("errorCode", FestivalErrorCode.FESTIVAL_NOT_FOUND);
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
                    .admin(admin)
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
                    .hasFieldOrPropertyWithValue("errorCode", FestivalErrorCode.FESTIVAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getFestivals 메소드는")
    class Describe_getFestivals {

        @Test
        @DisplayName("페이지네이션된 축제 목록을 반환한다.")
        void it_returns_paginated_festival_list() {
            // Given
            List<Festival> festivals = createFestivals(admin, 15);
            int pageSize = 10;

            // When
            KeySetPageResponse<FestivalListResponse> firstPage = festivalService.getFestivals(null, null, pageSize);

            // Then
            assertAll(
                    () -> assertThat(firstPage.content()).hasSize(pageSize),
                    () -> assertThat(firstPage.cursor()).isNotNull(),
                    () -> assertThat(firstPage.hasNext()).isTrue(),
                    () -> assertThat(firstPage.content().get(0).festivalId()).isEqualTo(festivals.get(0).getId()),
                    () -> assertThat(firstPage.content().get(9).festivalId()).isEqualTo(festivals.get(9).getId())
            );

            // When
            KeySetPageResponse<FestivalListResponse> secondPage = festivalService.getFestivals(
                    firstPage.cursor().time(),
                    firstPage.cursor().id(),
                    pageSize
            );

            // Then
            assertAll(
                    () -> assertThat(secondPage.content()).hasSize(5),
                    () -> assertThat(secondPage.cursor()).isNull(),
                    () -> assertThat(secondPage.hasNext()).isFalse(),
                    () -> assertThat(secondPage.content().get(0).festivalId()).isEqualTo(festivals.get(10).getId()),
                    () -> assertThat(secondPage.content().get(4).festivalId()).isEqualTo(festivals.get(14).getId())
            );
        }

        @Test
        @DisplayName("빈 결과를 요청하면 빈 리스트와 null 커서를 반환한다.")
        void it_returns_empty_list_and_null_cursor_for_empty_result() {
            // Given
            createFestivals(admin, 0);
            int pageSize = 10;

            // When
            KeySetPageResponse<FestivalListResponse> response = festivalService.getFestivals(null, null, pageSize);

            // Then
            assertAll(
                    () -> assertThat(response.content()).isEmpty(),
                    () -> assertThat(response.cursor()).isNull(),
                    () -> assertThat(response.hasNext()).isFalse()
            );
        }

        @Test
        @DisplayName("페이지 크기가 전체 결과보다 큰 경우 모든 결과를 반환하고 다음 페이지가 없음을 표시한다.")
        void it_returns_all_results_when_page_size_is_larger() {
            // Given
            List<Festival> festivals = createFestivals(admin, 5);
            int pageSize = 10;

            // When
            KeySetPageResponse<FestivalListResponse> response = festivalService.getFestivals(null, null, pageSize);

            // Then
            assertAll(
                    () -> assertThat(response.content()).hasSize(5),
                    () -> assertThat(response.cursor()).isNull(),
                    () -> assertThat(response.hasNext()).isFalse(),
                    () -> assertThat(response.content().get(0).festivalId()).isEqualTo(festivals.get(0).getId()),
                    () -> assertThat(response.content().get(4).festivalId()).isEqualTo(festivals.get(4).getId())
            );
        }

        @Test
        @DisplayName("커서를 사용하여 중간 페이지를 요청할 수 있다.")
        void it_can_request_middle_page_using_cursor() {
            // Given
            List<Festival> festivals = createFestivals(admin, 25);
            int pageSize = 10;

            // When
            KeySetPageResponse<FestivalListResponse> firstPage = festivalService.getFestivals(null, null, pageSize);
            KeySetPageResponse<FestivalListResponse> secondPage = festivalService.getFestivals(
                    firstPage.cursor().time(),
                    firstPage.cursor().id(),
                    pageSize
            );

            // Then
            assertAll(
                    () -> assertThat(secondPage.content()).hasSize(pageSize),
                    () -> assertThat(secondPage.cursor()).isNotNull(),
                    () -> assertThat(secondPage.hasNext()).isTrue(),
                    () -> assertThat(secondPage.content().get(0).festivalId()).isEqualTo(festivals.get(10).getId()),
                    () -> assertThat(secondPage.content().get(9).festivalId()).isEqualTo(festivals.get(19).getId())
            );
        }

        @Test
        @DisplayName("시작 시간이 동일한 축제들을 ID순으로 정렬한다.")
        void it_sorts_festivals_with_same_start_time_by_id_desc() {
            // Given
            LocalDateTime sameStartTime = LocalDateTime.now().plusDays(1);
            List<Festival> festivals = createFestivalsWithSameStartTime(admin, 5, sameStartTime);
            int pageSize = 10;

            // When
            KeySetPageResponse<FestivalListResponse> response = festivalService.getFestivals(null, null, pageSize);

            // Then
            assertAll(
                    () -> assertThat(response.content()).hasSize(5),
                    () -> assertThat(response.cursor()).isNull(),
                    () -> assertThat(response.hasNext()).isFalse(),
                    () -> assertThat(response.content().get(0).festivalId()).isEqualTo(festivals.get(0).getId()),
                    () -> assertThat(response.content().get(4).festivalId()).isEqualTo(festivals.get(4).getId())
            );
        }

        private List<Festival> createFestivals(Member admin, int count) {
            LocalDateTime now = LocalDateTime.now();
            return festivalRepository.saveAll(
                    IntStream.range(0, count)
                            .mapToObj(i -> Festival.builder()
                                    .admin(admin)
                                    .title("페스티벌 " + i)
                                    .description("페스티벌 설명 " + i)
                                    .startTime(now.plusDays(i + 1))
                                    .endTime(now.plusDays(i + 8))
                                    .festivalPublicationStatus(FestivalPublicationStatus.PUBLISHED)
                                    .build()
                            )
                            .toList()
            );
        }

        private List<Festival> createFestivalsWithSameStartTime(Member admin, int count,
                                                                LocalDateTime startTime) {
            return festivalRepository.saveAll(
                    IntStream.range(0, count)
                            .mapToObj(i -> Festival.builder()
                                    .admin(admin)
                                    .title("페스티벌 " + i)
                                    .description("페스티벌 설명 " + i)
                                    .startTime(startTime)
                                    .endTime(startTime.plusDays(7))
                                    .festivalPublicationStatus(FestivalPublicationStatus.PUBLISHED)
                                    .build()
                            )
                            .toList()
            );
        }
    }
}