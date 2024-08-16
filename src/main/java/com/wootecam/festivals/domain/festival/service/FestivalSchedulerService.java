package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

/**
 * 축제의 시작 시간과 종료 시간을 스케줄링하는 서비스입니다.
 */
@Slf4j
@Service
public class FestivalSchedulerService {

    private final ScheduledTaskRegistrar taskRegistrar;
    private final FestivalRepository festivalRepository;
    private final TaskScheduler taskScheduler;
    private final FestivalStatusUpdateService festivalStatusUpdateService;

    public FestivalSchedulerService(FestivalStatusUpdateService festivalStatusUpdateService,
                                    TaskScheduler taskScheduler,
                                    FestivalRepository festivalRepository) {
        this.festivalStatusUpdateService = festivalStatusUpdateService;
        this.taskScheduler = taskScheduler;
        this.taskRegistrar = new ScheduledTaskRegistrar();
        this.taskRegistrar.setTaskScheduler(taskScheduler);
        this.festivalRepository = festivalRepository;
    }

    /**
     * 서버 재시작 시 모든 축제의 시작 시간과 종료 시간을 스케줄링합니다. 이미 지난 경우 직접 상태를 변경합니다. 지나지 않은 이벤트의 경우 시작 시간과 종료 시간을 스케줄링합니다.
     */
    @PostConstruct
    public void scheduleAllFestivals() {
        List<Festival> festivals = festivalRepository.findFestivalsWithRestartScheduler();
        LocalDateTime now = LocalDateTime.now();
        for (Festival festival : festivals) {
            if (festival.getEndTime().isBefore(now)) {
                festivalRepository.bulkUpdateFestivalStatusFestivals(FestivalPublicationStatus.COMPLETED,
                        LocalDateTime.now());
            } else if (festival.getStartTime().isBefore(now) && festival.getEndTime().isAfter(now)) {
                // 축제가 진행 중인 경우
                festivalRepository.bulkUpdateFestivalStatusFestivals(FestivalPublicationStatus.ONGOING,
                        LocalDateTime.now());
                scheduleEndTimeUpdate(festival);
            } else {
                // 축제가 아직 시작되지 않은 경우
                scheduleStatusUpdate(festival);
            }
        }
    }

    /**
     * 축제의 시작 시간과 종료 시간을 스케줄링합니다. FestivalService에서 축제를 생성할 때 호출됩니다.
     *
     * @param festival
     */
    public void scheduleStatusUpdate(Festival festival) {
        log.debug("Festival 스케줄링 - ID: {}", festival.getId());
        log.debug("현재 시간 : {}", LocalDateTime.now());
        log.debug("시작 시간 : {}", festival.getStartTime());
        log.debug("종료 시간 : {}", festival.getEndTime());
        scheduleStartTimeUpdate(festival);
        scheduleEndTimeUpdate(festival);
    }

    /**
     * 축제의 시작 시간을 스케줄링합니다.
     *
     * @param festival
     */
    private void scheduleStartTimeUpdate(Festival festival) {
        LocalDateTime startTime = festival.getStartTime();
        // startTime을 cron 표현식으로 변환합니다.
        String cronExpression = String.format("%d %d %d %d %d ?",
                startTime.getSecond(), startTime.getMinute(), startTime.getHour(),
                startTime.getDayOfMonth(), startTime.getMonthValue());

        taskScheduler.schedule(
                () -> festivalStatusUpdateService.updateFestivalStatus(festival.getId(),
                        FestivalPublicationStatus.ONGOING),
                new CronTrigger(cronExpression)
        );
    }

    private void scheduleEndTimeUpdate(Festival festival) {
        LocalDateTime endTime = festival.getEndTime();
        String cronExpression = String.format("%d %d %d %d %d ?",
                endTime.getSecond(), endTime.getMinute(), endTime.getHour(),
                endTime.getDayOfMonth(), endTime.getMonthValue());

        taskScheduler.schedule(
                () -> festivalStatusUpdateService.updateFestivalStatus(festival.getId(),
                        FestivalPublicationStatus.COMPLETED),
                new CronTrigger(cronExpression)
        );
    }
}
