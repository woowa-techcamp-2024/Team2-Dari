package com.wootecam.festivals.domain.festival.service;

import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

@ExtendWith(MockitoExtension.class)
@Nested
@DisplayName("FestivalSchedulerService 클래스는 ")
class FestivalSchedulerServiceTest {

    @Mock
    private FestivalStatusUpdateService festivalStatusUpdateService;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private FestivalRepository festivalRepository;

    @InjectMocks
    private FestivalSchedulerService festivalSchedulerService;

    @Test
    @DisplayName("scheduleAllFestivals 메서드는 모든 축제의 시작 시간과 종료 시간을 스케줄링한다.")
    void testScheduleAllFestivals() {
        // Given
        Festival festival = FestivalStub.createFestivalWithTime(LocalDateTime.now().plusMinutes(1),
                LocalDateTime.now().plusMinutes(2));

        List<Festival> festivals = Arrays.asList(festival);
        when(festivalRepository.findFestivalsWithRestartScheduler()).thenReturn(festivals);

        // Capture the scheduled tasks
        ArgumentCaptor<Runnable> runnableCaptor = forClass(Runnable.class);

        // When
        festivalSchedulerService.scheduleAllFestivals();

        // Then
        verify(festivalRepository).findFestivalsWithRestartScheduler();
        verify(taskScheduler, times(2)).schedule(runnableCaptor.capture(), any(CronTrigger.class));

        // Execute the captured tasks
        List<Runnable> capturedRunnables = runnableCaptor.getAllValues();
        for (Runnable runnable : capturedRunnables) {
            runnable.run();
        }

        verify(festivalStatusUpdateService, times(1)).updateFestivalStatus(festival.getId(),
                FestivalPublicationStatus.ONGOING);
        verify(festivalStatusUpdateService, times(1)).updateFestivalStatus(festival.getId(),
                FestivalPublicationStatus.COMPLETED);
    }

    @Test
    @DisplayName("서버 재시작 시 모든 축제의 시작 시간과 종료 시간을 체크하고 업데이트, 스케줄링 한다.")
    void testScheduleAllFestivals_Restart() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Festival completedFestival = FestivalStub.createFestivalWithTime(now.minusDays(2), now.minusDays(1));

        Festival ongoingFestival = FestivalStub.createFestivalWithTime(now.minusDays(1), now.plusDays(1));

        Festival upcomingFestival = FestivalStub.createFestivalWithTime(now.plusDays(1), now.plusDays(2));

        List<Festival> festivals = Arrays.asList(completedFestival, ongoingFestival, upcomingFestival);
        when(festivalRepository.findFestivalsWithRestartScheduler()).thenReturn(festivals);

        // Capture the scheduled tasks
        ArgumentCaptor<Runnable> runnableCaptor = forClass(Runnable.class);

        // When
        festivalSchedulerService.scheduleAllFestivals();

        // Then
        verify(festivalRepository).findFestivalsWithRestartScheduler();
        verify(festivalRepository, times(2)).bulkUpdateFestivalStatusFestivals(any(FestivalPublicationStatus.class),
                any(LocalDateTime.class));
        verify(taskScheduler, times(3)).schedule(runnableCaptor.capture(), any(CronTrigger.class));

        // Execute the captured tasks
        List<Runnable> capturedRunnables = runnableCaptor.getAllValues();
        for (Runnable runnable : capturedRunnables) {
            runnable.run();
        }

        verify(festivalStatusUpdateService, times(1)).updateFestivalStatus(ongoingFestival.getId(),
                FestivalPublicationStatus.ONGOING);
        verify(festivalStatusUpdateService, times(2)).updateFestivalStatus(ongoingFestival.getId(),
                FestivalPublicationStatus.COMPLETED);
    }
}
