package com.wootecam.festivals.domain.purchase.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.wootecam.festivals.domain.ticket.repository.TicketStockCountRedisRepository;
import com.wootecam.festivals.utils.TestApplication;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class TicketStockRepositoryTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TicketStockCountRedisRepository ticketStockRepository;

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Nested
    @DisplayName("getTicketStockCount 메소드는")
    class Describe_getTicketStockCount {

        @Test
        @DisplayName("티켓 재고 수량을 정확히 반환한다")
        void it_returns_correct_ticket_stock_count() {
            // Given
            Long ticketId = 1L;
            Long expectedCount = 100L;

            IntStream.range(0, expectedCount.intValue())
                    .forEach(i -> ticketStockRepository.increaseTicketStockCount(ticketId));

            // When
            Long ticketStockCount = ticketStockRepository.getTicketStockCount(ticketId);

            // Then
            assertThat(ticketStockCount).isEqualTo(expectedCount);
        }

        @Test
        @DisplayName("존재하지 않는 티켓에 대해 null 를 반환한다")
        void it_throws_exception_for_non_existent_ticket() {
            // Given
            Long nonExistentTicketId = 999L;

            // When & Then
            Long ticketStockCount = ticketStockRepository.getTicketStockCount(nonExistentTicketId);

            assertNull(ticketStockCount);
        }
    }

    @Nested
    @DisplayName("setTicketStockCount 메소드는")
    class Describe_setTicketStockCount {

        @Test
        @DisplayName("티켓 재고 수량을 정확히 설정한다")
        void it_sets_ticket_stock_count_correctly() {
            // Given
            Long ticketId = 1L;
            Long count = 100L;

            // When
            ticketStockRepository.setTicketStockCount(ticketId, count);

            // Then
            String result = redisTemplate.opsForValue().get(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
                    + ticketStockRepository.TICKET_STOCK_COUNT_PREFIX);
            assertThat(result).isEqualTo(count.toString());
        }
    }

    @Nested
    @DisplayName("decreaseTicketStockCount 메소드는")
    class Describe_decreaseTicketStockCount {

        @Test
        @DisplayName("티켓 재고 수량을 1 감소시키고 결과를 반환한다")
        void it_decreases_ticket_stock_count_by_one_and_returns_result() {
            // Given
            Long ticketId = 1L;
            Long initialCount = 100L;
            redisTemplate.opsForValue()
                    .set(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
                            + ticketStockRepository.TICKET_STOCK_COUNT_PREFIX, initialCount.toString());

            // When
            Long result = ticketStockRepository.decreaseTicketStockCount(ticketId);

            // Then
            assertThat(result).isEqualTo(initialCount - 1);
        }

        @Test
        @DisplayName("재고가 0일 때 음수 값을 반환한다")
        void it_returns_negative_value_when_stock_is_zero() {
            // Given
            Long ticketId = 1L;
            Long initialCount = 0L;
            redisTemplate.opsForValue()
                    .set(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
                            + ticketStockRepository.TICKET_STOCK_COUNT_PREFIX, initialCount.toString());

            // When
            Long result = ticketStockRepository.decreaseTicketStockCount(ticketId);

            // Then
            assertThat(result).isEqualTo(-1L);
        }
    }

    @Nested
    @DisplayName("increaseTicketStockCount 메소드는")
    class Describe_increaseTicketStockCount {

        @Test
        @DisplayName("티켓 재고 수량을 1 증가시키고 결과를 반환한다")
        void it_increases_ticket_stock_count_by_one_and_returns_result() {
            // Given
            Long ticketId = 1L;
            Long initialCount = 100L;
            redisTemplate.opsForValue()
                    .set(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
                            + ticketStockRepository.TICKET_STOCK_COUNT_PREFIX, initialCount.toString());

            // When
            Long result = ticketStockRepository.increaseTicketStockCount(ticketId);

            // Then
            assertThat(result).isEqualTo(initialCount + 1);
        }
    }
}