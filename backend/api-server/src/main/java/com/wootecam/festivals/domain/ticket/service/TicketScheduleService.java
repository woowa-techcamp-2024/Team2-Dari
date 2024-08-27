package com.wootecam.festivals.domain.ticket.service;

import com.wootecam.festivals.domain.purchase.repository.TicketInfoRedisRepository;
import com.wootecam.festivals.domain.purchase.repository.TicketStockRedisRepository;
import com.wootecam.festivals.domain.ticket.dto.TicketResponse;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketScheduleService {

    private final TicketRepository ticketRepository;
    private final TicketStockRedisRepository ticketStockRedisRepository;
    private final TicketInfoRedisRepository ticketInfoRedisRepository;

    /**
     * 10분 이내에 판매 시작되는 티켓의 메타 정보를 Redis에 저장 (10분 주기)
     * - Ticket 의 startSaleTime, endSaleTime
     */
    @Scheduled(fixedDelay = 600000, initialDelay = 0)
    public void scheduleRedisTicketInfoUpdate() {
        log.debug("Redis에 저장된 티켓 정보 업데이트 시작 시각 {}", LocalDateTime.now());

        // 현재 시간 기준 10분 이내에 판매 시작되는 티켓 정보 조회
        List<TicketResponse> tickets = ticketRepository.findTicketsByStartSaleTimeBetweenRangeWithRemainStock(
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));

        for (TicketResponse ticket : tickets) {
            // Redis 에 티켓 정보 업데이트 (tickets:ticketId:startSaleTime, tickets:ticketId:endSaleTime)
            ticketInfoRedisRepository.setTicketInfo(ticket.id(), ticket.startSaleTime(), ticket.endSaleTime());

            log.debug("Redis에 저장된 티켓 정보 업데이트 - 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
        }
    }

    /**
     * 현재 판매중인 티켓에 대해 Redis에 저장된 티켓 남은 재고 count 를 주기적으로 업데이트 (1분 주기)
     * - TicketStock 의 남은 재고 count
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 0)
    public void scheduleRedisTicketRemainStockUpdate() {
        log.debug("Redis에 남은 티켓 재고 업데이트 시작 시각 {}", LocalDateTime.now());

        // 현재 판매 진행 중인 티켓 조회
        List<TicketResponse> tickets = ticketRepository.findSaleOngoingTicketsWithRemainStock(LocalDateTime.now());

        for (TicketResponse ticket : tickets) {
            if (ticket.remainStock() < 0) {
                log.error("티켓 재고가 음수입니다. 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
                continue;
            }

            // Redis 에 남은 티켓 재고 업데이트 (tickets:ticketId:ticketStocks:count)
            ticketStockRedisRepository.setTicketStockCount(ticket.id(), ticket.remainStock());

            log.debug("Redis에 저장된 티켓 남은 재고 count 업데이트 - 티켓 ID: {}, 남은 재고: {}", ticket.id(), ticket.remainStock());
        }
    }
}
