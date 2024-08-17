package com.wootecam.festivals.domain.festival.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootTest
@Nested
@DisplayName("FestivalSchedulerService 클래스는 ")
class FestivalSchedulerServiceTest {

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private FestivalSchedulerService festivalSchedulerService;

    @Autowired
    private MemberRepository memberRepository;

    private Member admin;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(festivalRepository);
        TestDBCleaner.clear(memberRepository);
        taskScheduler.getScheduledThreadPoolExecutor().getQueue().clear();
        admin = memberRepository.save(
                Member.builder()
                        .name("Test Organization")
                        .email("Test Detail")
                        .profileImg("Test profileImg")
                        .build());
    }

    @Test
    @DisplayName("페스티벌 스케줄링을 수행한다.")
    void testScheduleFestivals() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Festival festival = Festival.builder()
                .admin(admin)
                .title("테스트 축제")
                .description("축제 설명")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(7))
                .build();

        memberRepository.save(admin);
        festivalRepository.save(festival);

        // When
        festivalSchedulerService.scheduleStatusUpdate(festival);

        // Then
        // 크론 태스크에 2개의 태스크가 추가되어야 한다.
        assertThat(taskScheduler.getScheduledThreadPoolExecutor().getQueue()).hasSize(2);

        /**
         * 시작 시간의 크론 태스크가 실행되면 축제 상태가 ONGOING으로 변경된다.
         * 종료 시간의 크론 태스크가 실행되면 축제 상태가 COMPLETED로 변경된다.
         */
        taskScheduler.getScheduledThreadPoolExecutor().getQueue().forEach(runnable -> {
            runnable.run();
            assertThat(festivalRepository.findById(festival.getId()).get().getFestivalProgressStatus()).isIn(
                    FestivalProgressStatus.ONGOING, FestivalProgressStatus.COMPLETED);
        });
    }

    @Test
    @DisplayName("서버 재시작 시 모든 축제의 시작 시간과 종료 시간을 체크하고 업데이트, 스케줄링 한다.")
    void testScheduleAllFestivals_Restart() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        // 다가오는 축제
        Festival upcomingFestival = Festival.builder()
                .admin(admin)
                .title("다가오는 축제")
                .description("축제 설명")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(7))
                .build();
        // 진행중인 축제
        Festival ongoingFestival = Festival.builder()
                .admin(admin)
                .title("진행중인 축제")
                .description("축제 설명")
                .startTime(now)
                .endTime(now.plusDays(1))
                .build();
        // 종료된 축제 - 서버 시간에서 now는 DB 저장 시 이미 지났기 때문에 저장하고 로직 실행 시 COMPLETED로 변경되어야 한다.
        Festival completedFestival = Festival.builder()
                .admin(admin)
                .title("종료된 축제")
                .description("축제 설명")
                .startTime(now)
                .endTime(now)
                .build();

        memberRepository.save(admin);
        festivalRepository.saveAll(Arrays.asList(upcomingFestival, ongoingFestival, completedFestival));

        // When
        festivalSchedulerService.scheduleAllFestivals();

        // Then
        assertAll(
                () -> assertThat(
                        festivalRepository.findById(upcomingFestival.getId()).get().getFestivalProgressStatus())
                        .isEqualTo(FestivalProgressStatus.UPCOMING),
                () -> assertThat(festivalRepository.findById(ongoingFestival.getId()).get().getFestivalProgressStatus())
                        .isEqualTo(FestivalProgressStatus.ONGOING),
                () -> assertThat(
                        festivalRepository.findById(completedFestival.getId()).get().getFestivalProgressStatus())
                        .isEqualTo(FestivalProgressStatus.COMPLETED),
                () -> assertThat(taskScheduler.getScheduledThreadPoolExecutor().getQueue()).hasSize(3)
        );
    }

    @Test
    @DisplayName("크론 태스크를 등록 중 지난 크론탭이면 즉시 실행한다.")
    void testScheduleStatusChange_ImmediateExecution() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Festival festival = Festival.builder()
                .admin(admin)
                .title("테스트 축제")
                .description("축제 설명")
                .startTime(now)
                .endTime(now)
                .build();

        memberRepository.save(admin);
        festivalRepository.save(festival);

        // When
        festivalSchedulerService.scheduleStatusUpdate(festival);

        // Then
        assertThat(festivalRepository.findById(festival.getId()).get().getFestivalProgressStatus())
                .isEqualTo(FestivalProgressStatus.COMPLETED);
    }
}
