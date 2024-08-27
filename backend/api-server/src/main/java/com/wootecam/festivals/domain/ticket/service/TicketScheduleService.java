package com.wootecam.festivals.domain.ticket.service;

import com.wootecam.festivals.domain.purchase.repository.TicketInfoRedisRepository;
import com.wootecam.festivals.domain.purchase.repository.TicketStockRedisRepository;
import com.wootecam.festivals.domain.ticket.dto.TicketResponse;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketScheduleService {

    private final TicketRepository ticketRepository;
    private final TicketStockRedisRepository ticketStockRedisRepository;
    private final TicketInfoRedisRepository ticketInfoRedisRepository;
    private final ThreadPoolTaskScheduler taskScheduler;

    /**
     * 판매 진행중이거나 앞으로 판매될 티켓의 메타 정보를 Redis에 저장
     * - Ticket 의 startSaleTime, endSaleTime
     */
    @EventListener(ContextRefreshedEvent.class)
    public void scheduleRedisTicketInfoUpdate() {
        log.debug("Redis에 티켓 정보 업데이트 테스크 스케줄링 시작 시각 {}", LocalDateTime.now());

        List<TicketResponse> tickets = ticketRepository.findUpcomingAndOngoingSaleTickets(
                LocalDateTime.now());

        for (TicketResponse ticket : tickets) {
            LocalDateTime startSaleTime = ticket.startSaleTime();
            // 판매 진행까지 10분 초과 남았다면 schedule
            if (startSaleTime.isAfter(LocalDateTime.now().plusMinutes(10))) {
                taskScheduler.schedule(() -> updateRedisTicketInfo(ticket),
                        createUpdateRedisTicketInfoCronTrigger(ticket));
                log.debug("Redis에 티켓 업데이트 스케줄링 완료 - 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
            }
            // 그렇지 않다면 바로 업데이트
            else {
                updateRedisTicketInfo(ticket);
                log.debug("Redis에 티켓 정보 즉시 업데이트 완료 - 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
            }
        }
    }

    /**
     * 현재 판매중인 티켓에 대해 Redis에 저장된 티켓 남은 재고 count 를 주기적으로 업데이트 (1분 주기)
     * - TicketStock 의 남은 재고 count
     */
//    @Scheduled(fixedDelay = 60000, initialDelay = 0)
    @Scheduled(fixedDelay = 60000, initialDelay = 0)
    public void scheduleRedisTicketRemainStockUpdate() {
        log.debug("Redis에 남은 티켓 재고 업데이트 시작 시각 {}", LocalDateTime.now());

        // 현재 판매 진행 중인 티켓 조회
        List<TicketResponse> tickets = ticketRepository.findSaleOngoingTicketsWithRemainStock(LocalDateTime.now());
        log.debug("현재 판매 진행 중인 티켓 조회 - 티켓 개수: {}", tickets.size());

        for (TicketResponse ticket : tickets) {
            if (ticket.remainStock() < 0) {
                log.error("티켓 재고가 음수입니다. 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
                continue;
            }

            // Redis 에 남은 티켓 재고 업데이트 (tickets:ticketId:ticketStocks:count)
            ticketStockRedisRepository.setTicketStockCount(ticket.id(), ticket.remainStock());

            log.debug("Redis에 저장된 티켓 남은 재고 count 업데이트 - 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
        }

        log.debug("Redis에 남은 티켓 재고 업데이트 종료 Update 된 원소 개수 {}", tickets.size());
    }

    private void updateRedisTicketInfo(TicketResponse ticket) {
        // Redis 에 티켓 정보 업데이트 (tickets:ticketId:startSaleTime, tickets:ticketId:endSaleTime)
        ticketInfoRedisRepository.setTicketInfo(ticket.id(), ticket.startSaleTime(), ticket.endSaleTime());

        log.debug("Redis에 저장된 티켓 정보 업데이트 - 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
    }

    // 판매 시간 전이라면 판매 10분 전에 스케줄링
    private CronTrigger createUpdateRedisTicketInfoCronTrigger(TicketResponse ticket) {
        LocalDateTime startSaleTime = ticket.startSaleTime();
        LocalDateTime scheduledTime = startSaleTime.minusMinutes(10);

        String cronExpression = String.format("%d %d %d %d %d ?",
                scheduledTime.getSecond(),
                scheduledTime.getMinute(),
                scheduledTime.getHour(),
                scheduledTime.getDayOfMonth(),
                scheduledTime.getMonthValue());

        return new CronTrigger(cronExpression);
    }
}
