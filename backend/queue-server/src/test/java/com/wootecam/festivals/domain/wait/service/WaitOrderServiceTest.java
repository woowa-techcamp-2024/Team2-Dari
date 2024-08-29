package com.wootecam.festivals.domain.wait.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wootecam.festivals.domain.purchase.repository.TicketStockCountRedisRepository;
import com.wootecam.festivals.domain.ticket.repository.CurrentTicketWaitRedisRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketInfoRedisRepository;
import com.wootecam.festivals.domain.wait.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.exception.WaitErrorCode;
import com.wootecam.festivals.domain.wait.repository.PassOrderRedisRepository;
import com.wootecam.festivals.domain.wait.repository.WaitingRedisRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

@DisplayName("WaitOrderService 클래스")
@TestPropertySource(properties = {
        "wait.queue.pass-chunk-size=5",
})
class WaitOrderServiceTest extends SpringBootTestConfig {

    private final Long ticketId = 1L;
    private final Long loginMemberId = 1L;

    @Autowired
    private TicketStockCountRedisRepository ticketStockCountRedisRepository;
    @Autowired
    private WaitOrderService waitOrderService;
    @Autowired
    private WaitingRedisRepository waitingRepository;
    @Autowired
    private TicketInfoRedisRepository ticketInfoRedisRepository;
    @Autowired
    private PassOrderRedisRepository passOrderRedisRepository;
    @Autowired
    private CurrentTicketWaitRedisRepository currentTicketWaitRedisRepository;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        ticketInfoRedisRepository.setTicketInfo(ticketId, LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1));
        ticketStockCountRedisRepository.setTicketStockCount(ticketId, 10L);
    }

    @Nested
    @DisplayName("getWaitOrder 메소드는")
    class Describe_getWaitOrder {

        @Test
        @DisplayName("대기열에 없는 사용자가 대기열에 참가하고, 대기열 통과 가능하면 통과를 반환한다.")
        void it_returns_pass_when_user_not_in_queue_and_can_pass() {
            // Given: 사용자가 대기열에 존재하지 않음
            Long loginMemberId = 10L;
            Long curPassOrder = 5L;

            passOrderRedisRepository.set(ticketId, curPassOrder);

            for (int i = 0; i < 5; ++i) {
                waitOrderService.getWaitOrder(ticketId, (long) i, null);
            }

            // When: 사용자가 대기열에 참가하고, 대기열을 통과할 수 있는지 확인
            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, null);

            // Then: 통과 가능 여부와 대기열 순서를 확인
            assertThat(response.purchasable()).isTrue();
            assertThat(response.relativeWaitOrder()).isEqualTo(1L); // (6 - 5)
            assertThat(response.absoluteWaitOrder()).isEqualTo(6L);

            assertThat(waitingRepository.exists(ticketId, loginMemberId)).isTrue(); // 사용자가 대기열에 추가되었는지 확인
        }

        @Test
        @DisplayName("재고가 없으면 예외를 던진다.")
        void it_throws_exception_when_no_stock_remains() {
            // Given: 사용자가 대기열에 존재하며 재고가 없음
            Long currentPassOrder = 5L;
            passOrderRedisRepository.set(ticketId, currentPassOrder);
            ticketStockCountRedisRepository.setTicketStockCount(ticketId, 0L); // 재고를 0으로 설정

            // When, Then: 재고 부족으로 예외 발생 확인
            assertThatThrownBy(() -> waitOrderService.getWaitOrder(ticketId, loginMemberId, 6L))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", WaitErrorCode.NO_STOCK);
        }

        @Test
        @DisplayName("대기 순서가 잘못된 경우 예외를 던진다.")
        void it_throws_exception_when_invalid_wait_order() {
            // Given: 사용자가 대기열에 존재하고 잘못된 대기 순서가 전달된 경우
            waitingRepository.addWaiting(ticketId, loginMemberId);

            // When, Then: 잘못된 대기 순서로 인한 예외 발생 확인
            assertThatThrownBy(() -> waitOrderService.getWaitOrder(ticketId, loginMemberId, -1L))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", WaitErrorCode.INVALID_WAIT_ORDER);
        }

        @Test
        @DisplayName("대기열에서 이탈한 사용자가 다시 대기열에 참가하고 새로운 대기열 순서를 발급받는다.")
        void it_assigns_new_wait_order_when_user_exits_and_rejoins_queue() {
            // Given: 사용자가 대기열에서 이탈한 후 다시 참가하는 경우
            Long currentPassOrder = 10L;
            Long loginMemberId = 10L;
            passOrderRedisRepository.set(ticketId, currentPassOrder);
            waitingRepository.addWaiting(ticketId, loginMemberId);
            for (int i = 0; i < 5; ++i) {
                waitOrderService.getWaitOrder(ticketId, (long) i, null);
            }

            // When: 사용자가 새로운 대기열 순서를 발급받음
            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, 5L);

            // Then: 새로운 대기열 순서가 발급되었는지 확인
            Long newWaitOrder = waitingRepository.getSize(ticketId);
            assertThat(response.purchasable()).isFalse();
            assertThat(response.relativeWaitOrder()).isEqualTo(newWaitOrder - currentPassOrder);
            assertThat(response.absoluteWaitOrder()).isEqualTo(newWaitOrder);
        }

        @Test
        @DisplayName("대기열에 참가하는 사용자가 대기열 통과 가능하면 통과를 반환한다.")
        void it_returns_pass_when_new_user_and_can_pass() {
            // Given
            Long currentPassOrder = 0L;
            passOrderRedisRepository.set(ticketId, currentPassOrder);

            // When
            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, 2L);

            // Then
            assertThat(response.purchasable()).isTrue();
        }

        @Test
        @DisplayName("대기열에 참가하는 사용자가 대기열 통과할 수 없다면 통과 못함을 반환한다.")
        void it_returns_cannot_pass_when_new_user_and_cannot_pass() {
            // Given
            Long currentPassOrder = 5L;
            passOrderRedisRepository.set(ticketId, currentPassOrder);

            // When
            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, 12L);

            // Then
            assertThat(response.purchasable()).isFalse();
        }

        @Test
        @DisplayName("대기열에 있는 사용자가 대기열 통과 가능하면 통과를 반환한다.")
        void it_returns_pass_when_user_in_queue_and_can_pass() {
            // Given: 사용자가 대기열에 존재하고 대기열 통과 가능한 경우
            Long currentPassOrder = 5L;
            passOrderRedisRepository.set(ticketId, currentPassOrder);
            for (int i = 0; i < 5; ++i) {
                waitOrderService.getWaitOrder(ticketId, (long) i, null);
            }

            // When: 사용자가 대기열 통과 가능한지 확인
            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, 5L);

            // Then: 통과 가능 여부와 대기열 순서를 확인
            assertThat(response.purchasable()).isTrue();
            assertThat(response.relativeWaitOrder()).isEqualTo(0L);
            assertThat(response.absoluteWaitOrder()).isEqualTo(5L);
        }

        @Test
        @DisplayName("대기열에 있는 사용자가 대기열 통과할 수 없다면 통과 못함을 반환한다.")
        void it_returns_cannot_pass_when_user_in_queue_and_cannot_pass() {
            // Given: 사용자가 대기열에 존재하고 대기열 통과 가능한 경우
            Long currentPassOrder = 5L;
            passOrderRedisRepository.set(ticketId, currentPassOrder);
            for (int i = 0; i < 5; ++i) {
                waitOrderService.getWaitOrder(ticketId, (long) i, null);
            }

            // When: 사용자가 대기열 통과 가능한지 확인
            WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, 8L);

            // Then: 통과 가능 여부와 대기열 순서를 확인
            assertThat(response.purchasable()).isTrue();
            assertThat(response.relativeWaitOrder()).isEqualTo(3L);
            assertThat(response.absoluteWaitOrder()).isEqualTo(8L);
        }

        @Test
        @DisplayName("존재하지 않는 티켓 대기열 참가나 조회를 요청하면 예외를 던진다.")
        void it_throws_exception_when_invalid_ticket() {
            Long currentPassOrder = 5L;
            passOrderRedisRepository.set(ticketId, currentPassOrder);

            assertThatThrownBy(() -> waitOrderService.getWaitOrder(2L, loginMemberId, 8L))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", WaitErrorCode.INVALID_TICKET);
        }

        @Test
        @DisplayName("티켓 판매 시각이 아닐 때, 티켓 대기열 참가나 조회를 요청하면 예외를 던진다.")
        void it_throws_exception_when_invalid_ticket_sale_time() {
            ticketInfoRedisRepository.setTicketInfo(ticketId, LocalDateTime.now().minusDays(2),
                    LocalDateTime.now().minusMinutes(1));
            Long currentPassOrder = 5L;
            passOrderRedisRepository.set(ticketId, currentPassOrder);

            assertThatThrownBy(() -> waitOrderService.getWaitOrder(2L, loginMemberId, 8L))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", WaitErrorCode.INVALID_TICKET);
        }
    }

    @Nested
    @DisplayName("updateCurrentPassOrder 메소드는")
    class Describe_updateCurrentPassOrder {
        private Long ticketId1 = 1L;
        private Long ticketId2 = 2L;

        @BeforeEach
        void setUp() {
            currentTicketWaitRedisRepository.addCurrentTicketWait(ticketId1);
            currentTicketWaitRedisRepository.addCurrentTicketWait(ticketId2);
            for (int i = 0; i < 6; ++i) {
                waitingRepository.addWaiting(ticketId1, (long) i);
            }
            for (int i = 0; i < 11; ++i) {
                waitingRepository.addWaiting(ticketId2, (long) i);
            }
        }

        @Test
        @DisplayName("현재 진행 중인 티켓팅들의 대기열 범위를 갱신한다")
        void it_updates_current_pass_order() {
            // given
            passOrderRedisRepository.set(ticketId1, 0L);
            passOrderRedisRepository.set(ticketId2, 5L);

            // when
            waitOrderService.updateCurrentPassOrder();

            // then
            Long newPassOrder1 = passOrderRedisRepository.get(ticketId1);
            Long newPassOrder2 = passOrderRedisRepository.get(ticketId2);
            assertThat(newPassOrder1).isEqualTo(5L);
            assertThat(newPassOrder2).isEqualTo(10L);
        }
    }
}
