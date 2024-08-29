package com.wootecam.festivals.domain.purchase.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.ticket.repository.TicketStockCountRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("보상 트랜잭션 테스트")
class CompensationServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private TicketStockCountRedisRepository ticketStockCountRedisRepository;

    @InjectMocks
    private CompensationService compensationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("보상 처리 성공: Redis 재고 증가 및 MySQL 티켓 재고 점유 해제")
    void compensateFailedPurchase_Success() {
        // 이 테스트는 Redis 재고 증가와 MySQL 티켓 재고 점유 해제가 모두 성공적으로 수행되는 경우를 검증합니다.
        // Given
        String paymentId = "test-payment-id";
        Long ticketId = 1L;
        Long ticketStockId = 100L;

        when(ticketStockCountRedisRepository.increaseTicketStockCount(ticketId)).thenReturn(1L);
        when(jdbcTemplate.update(anyString(), any(Long.class))).thenReturn(1);

        // When
        compensationService.compensateFailedPurchase(paymentId, ticketId, ticketStockId);

        // Then
        verify(ticketStockCountRedisRepository).increaseTicketStockCount(ticketId);
        verify(jdbcTemplate).update(
                eq("UPDATE ticket_stock SET ticket_stock_member_id = NULL WHERE ticket_stock_id = ?"),
                eq(ticketStockId)
        );
    }

    @Test
    @DisplayName("Redis 재고 증가 실패 시 MySQL 작업 수행하지 않음")
    void compensateFailedPurchase_RedisException() {
        // 이 테스트는 Redis 재고 증가 작업이 실패할 경우, MySQL 작업이 수행되지 않는 것을 검증합니다.
        // Given
        String paymentId = "test-payment-id";
        Long ticketId = 1L;
        Long ticketStockId = 100L;

        doThrow(new RuntimeException("Redis error"))
                .when(ticketStockCountRedisRepository).increaseTicketStockCount(ticketId);

        // When
        compensationService.compensateFailedPurchase(paymentId, ticketId, ticketStockId);

        // Then
        verify(ticketStockCountRedisRepository).increaseTicketStockCount(ticketId);
        verify(jdbcTemplate, never()).update(anyString(), any(Long.class));
    }

    @Test
    @DisplayName("MySQL 티켓 재고 점유 해제 실패 시 예외 처리")
    void compensateFailedPurchase_JdbcException() {
        // 이 테스트는 Redis 재고 증가는 성공했지만 MySQL 티켓 재고 점유 해제가 실패할 경우의 예외 처리를 검증합니다.
        // Given
        String paymentId = "test-payment-id";
        Long ticketId = 1L;
        Long ticketStockId = 100L;

        when(ticketStockCountRedisRepository.increaseTicketStockCount(ticketId)).thenReturn(1L);
        doThrow(new DataAccessException("Database error") {
        })
                .when(jdbcTemplate).update(anyString(), any(Long.class));

        // When
        compensationService.compensateFailedPurchase(paymentId, ticketId, ticketStockId);

        // Then
        verify(ticketStockCountRedisRepository).increaseTicketStockCount(ticketId);
        verify(jdbcTemplate).update(
                eq("UPDATE ticket_stock SET ticket_stock_member_id = NULL WHERE ticket_stock_id = ?"),
                eq(ticketStockId)
        );
    }

    @Test
    @DisplayName("Redis와 MySQL 작업 모두 실패 시 예외 처리")
    void compensateFailedPurchase_BothOperationsFail() {
        // 이 테스트는 Redis 재고 증가와 MySQL 티켓 재고 점유 해제가 모두 실패할 경우의 예외 처리를 검증합니다.
        // Given
        String paymentId = "test-payment-id";
        Long ticketId = 1L;
        Long ticketStockId = 100L;

        doThrow(new RuntimeException("Redis error"))
                .when(ticketStockCountRedisRepository).increaseTicketStockCount(ticketId);
        doThrow(new DataAccessException("Database error") {
        })
                .when(jdbcTemplate).update(anyString(), any(Long.class));

        // When
        compensationService.compensateFailedPurchase(paymentId, ticketId, ticketStockId);

        // Then
        verify(ticketStockCountRedisRepository).increaseTicketStockCount(ticketId);
        verify(jdbcTemplate, never()).update(anyString(), any(Long.class));
    }
}