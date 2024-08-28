package com.wootecam.festivals.domain.wait.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wootecam.festivals.domain.purchase.repository.TicketStockCountRedisRepository;
import com.wootecam.festivals.domain.wait.PassOrder;
import com.wootecam.festivals.domain.wait.dto.WaitOrderCreateResponse;
import com.wootecam.festivals.domain.wait.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.exception.WaitErrorCode;
import com.wootecam.festivals.domain.wait.repository.WaitingRedisRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@DisplayName("WaitOrderService 클래스")
class WaitOrderServiceTest extends SpringBootTestConfig {

    private final Long ticketId = 1L;
    private final Long loginMemberId = 1L;

    @Autowired
    private TicketStockCountRedisRepository ticketStockCountRedisRepository;
    private final Integer passChunkSize = 10;
    @Autowired
    private WaitOrderService waitOrderService;
    @Autowired
    private WaitingRedisRepository waitingRepository;
    @Autowired
    private PassOrder passOrder;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Nested
    @DisplayName("createWaitOrder 메소드는")
    class Describe_createWaitOrder {

        @Nested
        @DisplayName("유저가 이미 대기열에 있는 경우")
        class Context_with_already_waiting_user {

            @BeforeEach
            void setUp() {
                waitingRepository.addWaiting(ticketId, loginMemberId);
            }

            @Test
            @DisplayName("예외를 던진다")
            void it_throws_api_exception() {
                ApiException apiException = assertThrows(ApiException.class,
                        () -> waitOrderService.createWaitOrder(ticketId, loginMemberId));
                assertThat(apiException.getErrorCode()).isEqualTo(WaitErrorCode.ALREADY_WAITING);
            }
        }

        @Nested
        @DisplayName("유저가 대기열에 없는 경우")
        class Context_with_not_waiting_user {

            @Test
            @DisplayName("대기열 블록 순서를 반환한다")
            void it_returns_wait_order_create_response() {
                WaitOrderCreateResponse response = waitOrderService.createWaitOrder(ticketId, loginMemberId);

                assertThat(response.waitOrderBlock()).isEqualTo(1L);
                assertThat(waitingRepository.exists(ticketId, loginMemberId)).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("getWaitOrder 메소드는")
    class Describe_getWaitOrder {

        @Nested
        @DisplayName("유저가 대기열에 없는 경우")
        class Context_with_not_waiting_user {

            @Test
            @DisplayName("예외를 반환한다")
            void it_throws_api_exception() {
                ApiException apiException = assertThrows(ApiException.class,
                        () -> waitOrderService.getWaitOrder(ticketId, loginMemberId, 1L));
                assertThat(apiException.getErrorCode()).isEqualTo(WaitErrorCode.CANNOT_FOUND_USER);
            }
        }

        @Nested
        @DisplayName("유저가 대기열에 있고, 유효한 대기열 순서를 가진 경우")
        class Context_with_valid_wait_order_block {

            @BeforeEach
            void setUp() {
                waitingRepository.addWaiting(ticketId, loginMemberId);
                passOrder.updateByWaitOrder(ticketId, 1L);
                ticketStockCountRedisRepository.setTicketStockCount(ticketId, 10L);
            }

            @Test
            @DisplayName("사용자가 구매 페이지로 진입할 수 있음을 반환한다")
            void it_returns_wait_order_response() {
                WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId, 1L);

                assertThat(response.purchasable()).isTrue();
                assertThat(response.waitOrder()).isEqualTo(0L);
            }
        }

        @Nested
        @DisplayName("대기열 순서가 잘못된 경우")
        class Context_with_invalid_wait_order_block {

            @BeforeEach
            void setUp() {
                waitingRepository.addWaiting(ticketId, loginMemberId);
            }

            @Test
            @DisplayName("예외를 반환한다")
            void it_throws_api_exception() {
                ApiException apiException = assertThrows(ApiException.class,
                        () -> waitOrderService.getWaitOrder(ticketId, loginMemberId, -1L));
                assertThat(apiException.getErrorCode()).isEqualTo(WaitErrorCode.INVALID_WAIT_ORDER);
            }
        }


        @Nested
        @DisplayName("재고가 부족한 경우")
        class Context_with_no_stock {

            @BeforeEach
            void setUp() {
                waitingRepository.addWaiting(ticketId, loginMemberId);
                passOrder.updateByWaitOrder(ticketId, 1L);
                ticketStockCountRedisRepository.setTicketStockCount(ticketId, 0L); // 재고 없음 설정
            }

            @Test
            @DisplayName("예외를 반환한다")
            void it_throws_api_exception() {
                ApiException apiException = assertThrows(ApiException.class,
                        () -> waitOrderService.getWaitOrder(ticketId, loginMemberId, 1L));
                assertThat(apiException.getErrorCode()).isEqualTo(WaitErrorCode.NO_STOCK);
            }
        }
    }

    @Nested
    @DisplayName("updateCurrentPassOrder 메소드는")
    class Describe_updateCurrentPassOrder {

        @BeforeEach
        void setUp() {
            waitingRepository.addWaiting(ticketId, loginMemberId);
        }

        @Test
        @DisplayName("현재 대기열 순서를 갱신한다")
        void it_updates_current_pass_order() {
            waitOrderService.updateCurrentPassOrder();

            Long updatedOrder = passOrder.get(ticketId);
            assertThat(updatedOrder).isEqualTo(1L);
        }
    }
}
