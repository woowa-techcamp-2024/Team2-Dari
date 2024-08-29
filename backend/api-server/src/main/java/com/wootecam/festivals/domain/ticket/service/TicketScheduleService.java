package com.wootecam.festivals.domain.ticket.service;

import com.wootecam.festivals.domain.ticket.dto.TicketResponse;
import com.wootecam.festivals.domain.ticket.repository.CurrentTicketWaitRedisRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketInfoRedisRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockCountRedisRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketScheduleService {

    private final TicketRepository ticketRepository;
    private final TicketInfoRedisRepository ticketInfoRedisRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final TicketStockCountRedisRepository ticketStockCountRedisRepository;
    private final CurrentTicketWaitRedisRepository currentTicketWaitRedisRepository;

    /**
     * 판매 진행중이거나 앞으로 판매될 티켓의 메타 정보와 재고를 Redis에 저장 - Ticket 의 startSaleTime, endSaleTime, remainStock
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
                taskScheduler.schedule(() -> updateRedisTicketInfo(ticket), createUpdateRedisTicketCronTrigger(ticket));
                taskScheduler.schedule(() -> updateRedisTicketStockCount(ticket),
                        createUpdateRedisTicketCronTrigger(ticket));
                log.debug("Redis 티켓 정보 업데이트 스케줄링 완료 - 티켓 ID: {}, 판매 시작 시각: {}, 판매 종료 시각: {}, 남은 재고: {}", ticket.id(),
                        ticket.startSaleTime(), ticket.endSaleTime(), ticket.remainStock());
            }
            // 그렇지 않다면 바로 업데이트
            else {
                updateRedisTicketInfo(ticket);
                updateRedisTicketStockCount(ticket);
                updateRedisCurrentTicketWait(ticket.id());

                log.debug("Redis 티켓 정보 즉시 업데이트 완료 - 티켓 ID: {}, 판매 시작 시각: {}, 판매 종료 시각: {}, 남은 재고: {}", ticket.id(),
                        ticket.startSaleTime(), ticket.endSaleTime(), ticket.remainStock());
            }
        }
    }

    private void updateRedisTicketInfo(TicketResponse ticket) {
        // Redis 에 티켓 정보 업데이트 (tickets:ticketId:startSaleTime, tickets:ticketId:endSaleTime)
        ticketInfoRedisRepository.setTicketInfo(ticket.id(), ticket.startSaleTime(), ticket.endSaleTime());

        log.debug("Redis 티켓 정보 업데이트 완료 - 티켓 ID: {}, 판매 시작 시각: {}, 판매 종료 시각: {}", ticket.id(), ticket.startSaleTime(),
                ticket.endSaleTime());
    }


    private void updateRedisTicketStockCount(TicketResponse ticket) {
        // Redis 에 남은 티켓 재고 업데이트 (tickets:ticketId:ticketStocks:count)
        ticketStockCountRedisRepository.setTicketStockCount(ticket.id(), ticket.remainStock());

        log.debug("Redis에 저장된 티켓 남은 재고 count 업데이트 - 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
    }

    private void updateRedisCurrentTicketWait(Long ticketId) {
        currentTicketWaitRedisRepository.addCurrentTicketWait(ticketId);

        log.debug("Redis에 진행할 티켓 ID 추가 - 티켓 ID: {}", ticketId);
    }


    // 판매 시간 전이라면 판매 10분 전에 스케줄링
    private CronTrigger createUpdateRedisTicketCronTrigger(TicketResponse ticket) {
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
