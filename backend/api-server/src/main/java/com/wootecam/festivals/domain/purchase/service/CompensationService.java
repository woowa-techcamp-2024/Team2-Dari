package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.domain.ticket.repository.TicketStockCountRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompensationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final TicketStockCountRedisRepository ticketStockCountRedisRepository;

    @Transactional
    public void compensateFailedPurchase(String paymentId, Long ticketId, Long ticketStockId) {
        try {
            // 레디스 재고 복구
            ticketStockCountRedisRepository.increaseTicketStockCount(ticketId);
            // MySQL의 TicketStock 점유 해제
            jdbcTemplate.update("UPDATE ticket_stock SET ticket_stock_member_id = NULL WHERE ticket_stock_id = ?",
                    ticketStockId);

        } catch (Exception e) {
            log.error("Compensation failed for paymentId: {}", paymentId, e);
        }
    }
}
