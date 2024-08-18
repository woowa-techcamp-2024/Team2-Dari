package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 축제의 시작 시간과 종료 시간을 스케줄링하는 서비스입니다. 서버 재시작 시 모든 축제의 상태를 갱신하고 향후 상태 변경을 스케줄링합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalSchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final FestivalRepository festivalRepository;
    private final FestivalStatusUpdateService festivalStatusUpdateService;

    /**
     * 서버 재시작 시 모든 축제의 상태를 갱신하고 향후 상태 변경을 스케줄링합니다. 이미 종료된 축제는 완료 상태로 변경하고, 진행 중인 축제는 진행 중 상태로 변경합니다. 아직 시작하지 않은 축제는 시작 및
     * 종료 시간을 스케줄링합니다.
     */
    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void scheduleAllFestivals() {
        LocalDateTime now = LocalDateTime.now();

        // 완료된 축제는 완료 상태로 벌크성 쿼리로 변경
        festivalRepository.bulkUpdateCOMPLETEDFestivals(now);
        // 진행 중인 축제는 진행 중 상태로 벌크성 쿼리로 변경
        festivalRepository.bulkUpdateONGOINGFestivals(now);

        List<Festival> festivals = festivalRepository.findFestivalsWithRestartScheduler();

        log.debug("축제 스케줄링 시작. 총 {} 개의 축제가 대상입니다.", festivals.size());

        for (Festival festival : festivals) {
            if (festival.getStartTime().isAfter(now)) {
                scheduleStatusUpdate(festival);
            } else if (festival.getStartTime().isBefore(now) && festival.getEndTime().isAfter(now)) {
                scheduleEndTimeUpdate(festival);
            }
        }

        log.debug("축제 스케줄링 완료.");
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
        scheduleStatusChange(festival, FestivalProgressStatus.ONGOING, festival.getStartTime(), "시작");
    }

    private void scheduleEndTimeUpdate(Festival festival) {
        scheduleStatusChange(festival, FestivalProgressStatus.COMPLETED, festival.getEndTime(), "종료");
    }

    private void scheduleStatusChange(Festival festival, FestivalProgressStatus status, LocalDateTime scheduledTime,
                                      String eventType) {
        LocalDateTime now = LocalDateTime.now();
        if (scheduledTime.isBefore(now)) {
            // 이미 지난 시간의 경우 즉시 실행
            festivalStatusUpdateService.updateFestivalStatus(festival.getId(), status);
            log.info("축제 ID: {}의 {} 스케줄링되어 상태가 {}로 즉시 변경되었습니다.", festival.getId(), eventType, status);
        } else {
            // 미래의 시간에 대해 스케줄링
            String cronExpression = createCronExpression(scheduledTime);
            taskScheduler.schedule(
                    () -> {
                        festivalStatusUpdateService.updateFestivalStatus(festival.getId(), status);
                        log.info("축제 ID: {}의 {} 스케줄링되어 상태가 {}로 변경되었습니다.", festival.getId(), eventType, status);
                    },
                    new CronTrigger(cronExpression)
            );
            log.debug("축제 ID: {}의 {} 시간이 {}으로 스케줄링되었습니다.", festival.getId(), eventType, cronExpression);
            log.debug("현재 등록된 스케줄러 개수: {}", taskScheduler.getScheduledThreadPoolExecutor().getQueue().size());
        }
    }

    private String createCronExpression(LocalDateTime dateTime) {
        return String.format("%d %d %d %d %d ?",
                dateTime.getSecond(), dateTime.getMinute(), dateTime.getHour(),
                dateTime.getDayOfMonth(), dateTime.getMonthValue());
    }
}
