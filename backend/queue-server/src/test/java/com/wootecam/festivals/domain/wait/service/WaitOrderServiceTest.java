package com.wootecam.festivals.domain.wait.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.purchase.repository.TicketStockCountRedisRepository;
import com.wootecam.festivals.domain.wait.PassOrder;
import com.wootecam.festivals.domain.wait.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.repository.WaitingRedisRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "wait.queue.pass-chunk-size=3"
})
public class WaitOrderServiceTest extends SpringBootTestConfig {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private WaitOrderService waitOrderService;

    private final Long ticketId = 1L;

    @Autowired
    private TicketStockCountRedisRepository ticketStockCountRedisRepository;
    @Autowired
    private WaitingRedisRepository waitingRepository;
    @Autowired
    private PassOrder passOrder;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        ticketStockCountRedisRepository.setTicketStockCount(ticketId, 10L);
    }

    @Nested
    @DisplayName("getWaitOrder 메소드는")
    class Describe_getWaitOrder {

        @Test
        @DisplayName("대기열에 아무도 없는 경우, 대기열에 사용자가 들어오면, 대기열 순서를 1로 반환한다.")
        void testGetWaitOrder_NewUser() {
            Long loginMemberId = 100L;

            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, null);

            assertAll(() -> assertThat(response.purchasable()).isFalse(),
                    () -> assertThat(response.waitOrder()).isEqualTo(1L));
        }

        @Test
        @DisplayName("대기열에 존재하는 사용자이고, 입장 순서가 되지 않았다면, 입장 미허용 및 사용자의 대기열 순서를 반환한다.")
        void testGetWaitOrder_ExistingUser() {
            Long loginMemberId = 2L;
            Long waitOrder = 3L;

            waitingRepository.addWaiting(ticketId, loginMemberId);
            passOrder.set(ticketId, 1L);

            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, waitOrder);

            assertAll(() -> assertThat(response.purchasable()).isFalse(),
                    () -> assertThat(response.waitOrder()).isEqualTo(waitOrder));
        }

        @Test
        @DisplayName("대기열에 존재하는 사용자이고, 유효한 입장 순서라면, 입장 허용 및 사용자의 대기열 순서를 반환한다.")
        void testGetWaitOrder_ValidWaitOrder() {
            Long loginMemberId = 100L;
            Long waitOrder = 1L;

            waitingRepository.addWaiting(ticketId, loginMemberId);

            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, waitOrder);

            assertAll(() -> assertThat(response.purchasable()).isTrue(),
                    () -> assertThat(response.waitOrder()).isEqualTo(waitOrder));
        }

        @Test
        @DisplayName("대기열에 존재하는 사용자이고, 유효하지 않은 입장 순서라면, 예외를 반환한다")
        void testGetWaitOrder_InvalidWaitOrder() {
            Long loginMemberId = 100L;
            Long waitOrder = -1L;

            waitingRepository.addWaiting(ticketId, loginMemberId);

            assertThatThrownBy(() ->
                    waitOrderService.getWaitOrder(ticketId, loginMemberId, waitOrder))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        @DisplayName("첫 번째 대기자가 들어오면, 첫 번째 대기 순서를 반환한다.")
        void it_returns_1_when_no_waiting() {
            // given
            Long ticketId = 1L;
            Long loginMemberId = 100L;
            Long waitOrder = null;

            // when
            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, waitOrder);

            // then
            assertAll(() -> assertThat(response.waitOrder()).isEqualTo(1L));
        }
    }
}