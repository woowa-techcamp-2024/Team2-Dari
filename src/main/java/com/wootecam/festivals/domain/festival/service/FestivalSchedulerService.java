package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
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
 * 축제의 시작 시간과 종료 시간을 스케줄링하는 서비스입니다. 서버 재시작 시 모든 축제의 상태를 갱신하고 향후 상태 변경을 스케줄링합니다.
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
     * 서버 재시작 시 모든 축제의 상태를 갱신하고 향후 상태 변경을 스케줄링합니다. 이미 종료된 축제는 완료 상태로 변경하고, 진행 중인 축제는 진행 중 상태로 변경합니다. 아직 시작하지 않은 축제는 시작 및
     * 종료 시간을 스케줄링합니다.
     */
    @PostConstruct
    public void scheduleAllFestivals() {
        List<Festival> festivals = festivalRepository.findFestivalsWithRestartScheduler();
        LocalDateTime now = LocalDateTime.now();

        log.debug("축제 스케줄링 시작. 총 {} 개의 축제가 대상입니다.", festivals.size());

        for (Festival festival : festivals) {
            if (festival.getEndTime().isBefore(now)) {
                updateCompletedFestival(festival);
            } else if (festival.getStartTime().isBefore(now) && festival.getEndTime().isAfter(now)) {
                // 축제가 진행 중인 경우
                updateOngoingFestival(festival);
            } else {
                // 축제가 아직 시작되지 않은 경우
                scheduleStatusUpdate(festival);
            }
        }

        log.debug("축제 스케줄링 완료.");
    }

    private void updateCompletedFestival(Festival festival) {
        festivalRepository.bulkUpdateFestivalStatusFestivals(FestivalProgressStatus.COMPLETED, LocalDateTime.now());
        log.info("축제 ID: {}가 이미 종료되어 완료 상태로 변경되었습니다.", festival.getId());
    }

    private void updateOngoingFestival(Festival festival) {
        festivalRepository.bulkUpdateFestivalStatusFestivals(FestivalProgressStatus.ONGOING, LocalDateTime.now());
        scheduleEndTimeUpdate(festival);
        log.info("축제 ID: {}가 진행 중 상태로 변경되었으며, 종료 시간이 스케줄링되었습니다.", festival.getId());
    }

    /**
     * 축제의 시작 시간과 종료 시간을 스케줄링합니다. FestivalService에서 축제를 생성할 때 호출됩니다.
     *
     * @param festival 스케줄링할 축제
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
        String cronExpression = createCronExpression(festival.getStartTime());
        scheduleStatusChange(festival, FestivalProgressStatus.ONGOING, cronExpression, "시작");
    }

    private void scheduleEndTimeUpdate(Festival festival) {
        String cronExpression = createCronExpression(festival.getEndTime());
        scheduleStatusChange(festival, FestivalProgressStatus.COMPLETED, cronExpression, "종료");
    }

    private void scheduleStatusChange(Festival festival, FestivalProgressStatus status, String cronExpression,
                                      String eventType) {
        taskScheduler.schedule(
                () -> {
                    festivalStatusUpdateService.updateFestivalStatus(festival.getId(), status);
                    log.info("축제 ID: {}의 {}가 스케줄링되어 상태가 {}로 변경되었습니다.", festival.getId(), eventType, status);
                },
                new CronTrigger(cronExpression)
        );
        log.debug("축제 ID: {}의 {} 시간이 {}으로 스케줄링되었습니다.", festival.getId(), eventType, cronExpression);
    }

    private String createCronExpression(LocalDateTime dateTime) {
        return String.format("%d %d %d %d %d ?",
                dateTime.getSecond(), dateTime.getMinute(), dateTime.getHour(),
                dateTime.getDayOfMonth(), dateTime.getMonthValue());
    }
}
