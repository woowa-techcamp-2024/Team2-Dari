//package com.wootecam.festivals.domain.purchase.repository;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertNull;
//
//import com.wootecam.festivals.domain.purchase.TicketStockRepository;
//import java.util.stream.IntStream;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//
//@SpringBootTest
//@DisplayName("TicketStockRepository 테스트")
//class TicketStockRepositoryTest {
//
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Autowired
//    private TicketStockRepository ticketStockRepository;
//
//    @AfterEach
//    void tearDown() {
//        redisTemplate.getConnectionFactory().getConnection().flushAll();
//    }
//
//    @Nested
//    @DisplayName("getTicketStockCount 메소드는")
//    class Describe_getTicketStockCount {
//
//        @Test
//        @DisplayName("티켓 재고 수량을 정확히 반환한다")
//        void it_returns_correct_ticket_stock_count() {
//            // Given
//            Long ticketId = 1L;
//            Long expectedCount = 100L;
//
//            IntStream.range(0, expectedCount.intValue())
//                    .forEach(i -> ticketStockRepository.increaseTicketStockCount(ticketId));
//
//            // When
//            String result = ticketStockRepository.getTicketStockCount(ticketId);
//
//            // Then
//            assertThat(result).isEqualTo(String.valueOf(expectedCount));
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 티켓에 대해 null 를 반환한다")
//        void it_throws_exception_for_non_existent_ticket() {
//            // Given
//            Long nonExistentTicketId = 999L;
//
//            // When & Then
//            String nullValue = ticketStockRepository.getTicketStockCount(nonExistentTicketId);
//
//            assertNull(nullValue);
//        }
//    }
//
//    @Nested
//    @DisplayName("setTicketStockCount 메소드는")
//    class Describe_setTicketStockCount {
//
//        @Test
//        @DisplayName("티켓 재고 수량을 정확히 설정한다")
//        void it_sets_ticket_stock_count_correctly() {
//            // Given
//            Long ticketId = 1L;
//            Long count = 100L;
//
//            // When
//            ticketStockRepository.setTicketStockCount(ticketId, count);
//
//            // Then
//            String result = redisTemplate.opsForValue().get(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
//                    + ticketStockRepository.TICKET_STOCK_COUNT_PREFIX);
//            assertThat(result).isEqualTo(count.toString());
//        }
//    }
//
//    @Nested
//    @DisplayName("decreaseTicketStockCount 메소드는")
//    class Describe_decreaseTicketStockCount {
//
//        @Test
//        @DisplayName("티켓 재고 수량을 1 감소시키고 결과를 반환한다")
//        void it_decreases_ticket_stock_count_by_one_and_returns_result() {
//            // Given
//            Long ticketId = 1L;
//            Long initialCount = 100L;
//            redisTemplate.opsForValue()
//                    .set(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
//                            + ticketStockRepository.TICKET_STOCK_COUNT_PREFIX, initialCount.toString());
//
//            // When
//            Long result = ticketStockRepository.decreaseTicketStockCount(ticketId);
//
//            // Then
//            assertThat(result).isEqualTo(initialCount - 1);
//        }
//
//        @Test
//        @DisplayName("재고가 0일 때 음수 값을 반환한다")
//        void it_returns_negative_value_when_stock_is_zero() {
//            // Given
//            Long ticketId = 1L;
//            Long initialCount = 0L;
//            redisTemplate.opsForValue()
//                    .set(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
//                            + ticketStockRepository.TICKET_STOCK_COUNT_PREFIX, initialCount.toString());
//
//            // When
//            Long result = ticketStockRepository.decreaseTicketStockCount(ticketId);
//
//            // Then
//            assertThat(result).isEqualTo(-1L);
//        }
//    }
//
//    @Nested
//    @DisplayName("increaseTicketStockCount 메소드는")
//    class Describe_increaseTicketStockCount {
//
//        @Test
//        @DisplayName("티켓 재고 수량을 1 증가시키고 결과를 반환한다")
//        void it_increases_ticket_stock_count_by_one_and_returns_result() {
//            // Given
//            Long ticketId = 1L;
//            Long initialCount = 100L;
//            redisTemplate.opsForValue()
//                    .set(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
//                            + ticketStockRepository.TICKET_STOCK_COUNT_PREFIX, initialCount.toString());
//
//            // When
//            Long result = ticketStockRepository.increaseTicketStockCount(ticketId);
//
//            // Then
//            assertThat(result).isEqualTo(initialCount + 1);
//        }
//    }
//
//    @Nested
//    @DisplayName("removeTicketStock 메소드는")
//    class Describe_removeTicketStock {
//
//        @Test
//        @DisplayName("티켓 재고에서 하나를 제거하고 해당 원소를 반환한다")
//        void it_removes_one_ticket_stock_and_returns_its_id() {
//            // Given
//            Long ticketId = 1L;
//            Long ticketStockId = 100L;
//            redisTemplate.opsForSet().add(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
//                    + ticketStockRepository.TICKET_STOCKS_PREFIX, ticketStockId.toString());
//
//            // When
//            String result = ticketStockRepository.removeTicketStock(ticketId);
//
//            // Then
//            assertThat(result).isEqualTo(String.valueOf(ticketStockId));
//            assertThat(redisTemplate.opsForSet().size(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
//                    + ticketStockRepository.TICKET_STOCKS_PREFIX)).isZero();
//        }
//
//        @Test
//        @DisplayName("재고가 없을 때 null를 반환한다")
//        void it_throws_exception_when_no_stock_available() {
//            // Given
//            Long ticketId = 1L;
//
//            // When & Then
//            assertNull(ticketStockRepository.removeTicketStock(ticketId));
//        }
//    }
//
//    @Nested
//    @DisplayName("addTicketStock 메소드는")
//    class Describe_addTicketStock {
//
//        @Test
//        @DisplayName("티켓 재고를 추가하고 1를 반환한다")
//        void it_adds_ticket_stock_and_returns_success_status() {
//            // Given
//            Long ticketId = 1L;
//            Long ticketStockId = 100L;
//
//            // When
//            Long result = ticketStockRepository.addTicketStock(ticketId, ticketStockId);
//
//            // Then
//            assertThat(result).isEqualTo(1L); // Redis SET 명령어는 새로운 요소가 추가되면 1을 반환
//            assertThat(redisTemplate.opsForSet().size(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
//                    + ticketStockRepository.TICKET_STOCKS_PREFIX)).isEqualTo(1L);
//        }
//
//        @Test
//        @DisplayName("이미 존재하는 티켓 재고를 추가할 때 0을 반환한다")
//        void it_returns_zero_when_adding_existing_ticket_stock() {
//            // Given
//            Long ticketId = 1L;
//            Long ticketStockId = 100L;
//            ticketStockRepository.addTicketStock(ticketId, ticketStockId);
//
//            // When
//            Long result = ticketStockRepository.addTicketStock(ticketId, ticketStockId);
//
//            // Then
//            assertThat(result).isZero(); // 이미 존재하는 요소를 추가하면 0을 반환
//            assertThat(redisTemplate.opsForSet().size(ticketStockRepository.TICKETS_PREFIX + ticketId + ":"
//                    + ticketStockRepository.TICKET_STOCKS_PREFIX)).isEqualTo(1L);
//        }
//    }
//}