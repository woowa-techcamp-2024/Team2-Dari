package com.wootecam.festivals.domain.wait.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.purchase.repository.TicketStockCountRedisRepository;
import com.wootecam.festivals.domain.wait.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.repository.WaitingRedisRepository;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "purchasable.queue.size=3"
})
public class WaitOrderServiceTest extends SpringBootTestConfig {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private WaitOrderService waitOrderService;

    @Autowired
    private WaitingRedisRepository waitingRedisRepository;

    @Autowired
    private TicketStockCountRedisRepository ticketStockCountRedisRepository;

    @Value("${purchasable.queue.size}")
    private Integer purchasableQueueSize;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("대기열에 입장 가능 순서로 들어왔다면, 대기열을 통과할 수 있다")
    void getWaitOrder_WhenQueueIsShortAndStockAvailable_ReturnsSuccess() {
        // given
        Long ticketId = 1L;
        Long loginMemberId = 1L;

        ticketStockCountRedisRepository.setTicketStockCount(ticketId, 10L);

        // when
        WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId);

        // then
        assertAll(
                () -> assertThat(response.purchasable()).isTrue(),
                () -> assertThat(response.waitOrder()).isEqualTo(0L));
    }

    @Test
    @DisplayName("대기열에 입장 가능 순서보다 늦게 들어왔다면, 대기열을 통과할 수 없다")
    void getWaitOrder_WhenQueueIsLong_ReturnsFailure() {
        // given
        Long ticketId = 1L;
        Long loginMemberId = (long) purchasableQueueSize;

        ticketStockCountRedisRepository.setTicketStockCount(ticketId, 10L);

        for (int i = 0; i < purchasableQueueSize + 1; i++) {
            waitingRedisRepository.addWaiting(ticketId, (long) i);
        }

        // when
        WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId);

        assertAll(
                () -> assertThat(response.purchasable()).isFalse(),
                () -> assertThat(response.waitOrder()).isEqualTo(loginMemberId));
    }

    @Test
    @DisplayName("대기열에 입장 가능 순서로 들어와도 재고가 없다면, 대기열을 통과할 수 없다")
    void getWaitOrder_WhenStockUnavailable_ReturnsFailure() {
        //given
        Long ticketId = 1L;
        Long loginMemberId = 1L;

        ticketStockCountRedisRepository.setTicketStockCount(ticketId, 0L);

        //when
        WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, loginMemberId);

        //then
        assertThat(response.purchasable()).isFalse();
    }
}