package com.wootecam.festivals.domain.my.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.my.dto.MyFestivalCursor;
import com.wootecam.festivals.domain.my.dto.MyFestivalResponse;
import com.wootecam.festivals.global.page.CursorBasedPage;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("MyService 통합 테스트")
class MyServiceTest extends SpringBootTestConfig {

    private final MyService myService;
    private final FestivalRepository festivalRepository;
    private final MemberRepository memberRepository;

    private Member admin;

    @Autowired
    public MyServiceTest(MyService myService, FestivalRepository festivalRepository,
                         MemberRepository memberRepository) {
        this.myService = myService;
        this.festivalRepository = festivalRepository;
        this.memberRepository = memberRepository;
    }

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(festivalRepository);
        TestDBCleaner.clear(memberRepository);
        admin = memberRepository.save(
                Member.builder()
                        .name("Test Admin")
                        .email("Test Detail")
                        .profileImg("Test profileImg")
                        .build());
    }

    @Nested
    @DisplayName("내가 주최한 축제 목록 요청 시")
    class Describe_findHostedFestival {

        @Test
        @DisplayName("커서가 없다면 사용자가 개최한 축제 목록의 첫 페이지를 반환한다.")
        void it_returns_my_festival_list_first_page() {
            // Given
            Long loginMemberId = admin.getId();
            int count = 15;
            List<Festival> festivals = createFestivals(count);

            // When
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> firstPage = myService.findHostedFestival(
                    loginMemberId, null);

            // Then
            assertAll(
                    () -> assertThat(firstPage.getContent()).hasSize(10),
                    () -> assertThat(firstPage.getCursor()).isNotNull(),
                    () -> assertThat(firstPage.hasNext()).isTrue(),
                    () -> assertThat(firstPage.getContent().get(0).festivalId()).isEqualTo(
                            festivals.get(count - 1).getId()),
                    () -> assertThat(firstPage.getContent().get(9).festivalId()).isEqualTo(
                            festivals.get(count - 10).getId())
            );
        }

        @Test
        @DisplayName("커서가 있다면 사용자가 개최한 축제 목록 중 커서의 다음 페이지를 반환한다.")
        void it_returns_my_festival_list_next_page() {
            // Given
            Long loginMemberId = admin.getId();
            int count = 25;
            List<Festival> festivals = createFestivals(count);
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> firstPage = myService.findHostedFestival(
                    loginMemberId, null);
            MyFestivalCursor cursor = firstPage.getCursor();

            // When
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> secondPage = myService.findHostedFestival(
                    loginMemberId, cursor);

            // Then
            assertAll(
                    () -> assertThat(secondPage.getContent()).hasSize(10),
                    () -> assertThat(secondPage.getCursor()).isNotNull(),
                    () -> assertThat(secondPage.hasNext()).isTrue(),
                    () -> assertThat(secondPage.getContent().get(0).festivalId()).isEqualTo(
                            festivals.get(count - 11).getId()),
                    () -> assertThat(secondPage.getContent().get(9).festivalId()).isEqualTo(
                            festivals.get(count - 20).getId())
            );
        }

        @Test
        @DisplayName("개최한 축제가 없다면 빈 리스트와 null 커서를 반환한다.")
        void it_returns_empty_list_and_null_cursor_for_empty_result() {
            // Given
            Long loginMemberId = admin.getId();

            // When
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> response = myService.findHostedFestival(loginMemberId,
                    null);

            // Then
            assertAll(
                    () -> assertThat(response.getContent()).isEmpty(),
                    () -> assertThat(response.getCursor()).isNull(),
                    () -> assertThat(response.hasNext()).isFalse()
            );
        }

        @Test
        @DisplayName("페이지 크기가 전체 결과보다 크다면 모든 결과를 반환하고 다음 페이지가 없음을 표시한다.")
        void it_returns_all_results_when_page_size_is_larger() {
            // Given
            Long loginMemberId = admin.getId();
            int count = 5;
            List<Festival> festivals = createFestivals(count);

            // When
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> response = myService.findHostedFestival(loginMemberId,
                    null);

            // Then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(count),
                    () -> assertThat(response.getCursor()).isNull(),
                    () -> assertThat(response.hasNext()).isFalse(),
                    () -> assertThat(response.getContent().get(0).festivalId()).isEqualTo(
                            festivals.get(count - 1).getId()),
                    () -> assertThat(response.getContent().get(4).festivalId()).isEqualTo(
                            festivals.get(count - 5).getId())
            );
        }

        private List<Festival> createFestivals(int count) {
            LocalDateTime now = LocalDateTime.now();
            return IntStream.range(1, count + 1)
                    .mapToObj(i -> Festival.builder()
                            .admin(admin)
                            .title("페스티벌 " + i)
                            .description("페스티벌 설명 " + i)
                            .startTime(now.plusDays(i + 1))
                            .endTime(now.plusDays(i + 8))
                            .festivalPublicationStatus(FestivalPublicationStatus.PUBLISHED)
                            .build()
                    )
                    .map(festivalRepository::save)
                    .toList();
        }
    }
}