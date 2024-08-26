package com.wootecam.festivals.domain.wait.service;

import com.wootecam.festivals.domain.purchase.repository.TicketStockCountRedisRepository;
import com.wootecam.festivals.domain.wait.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.repository.WaitingRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitOrderService {

    private final WaitingRedisRepository waitingRepository;
    private final TicketStockCountRedisRepository ticketStockCountRedisRepository;

    @Value("${purchasable.queue.size}")
    private Integer PURCHASABLE_QUEUE_SIZE;

    /**
     * 사용자가 구매 페이지로 진입할 수 있는지를 대기 순서로 판단합니다.
     * 대기 순서는 0부터 시작하며, 대기 순서가 PURCHASABLE_QUEUE_SIZE보다 크거나 같다면 구매 페이지로 진입할 수 없습니다.
     * 진입할 수 있다면 재고를 차감하고, 대기열에서 삭제합니다.
     *
     * @param ticketId
     * @param loginMemberId
     * @return 사용자가 구매 페이지로 진입할 수 있는지 여부, 대기열 순서
     */
    public WaitOrderResponse getWaitOrder(Long ticketId, Long loginMemberId) {
        Long waitingCount = getWaitOrderInternal(ticketId, loginMemberId);

        if (waitingCount >= PURCHASABLE_QUEUE_SIZE) {
            return new WaitOrderResponse(false, waitingCount);
        }

        if (!ticketStockCountRedisRepository.checkAndDecreaseStock(ticketId, loginMemberId)) {
            return new WaitOrderResponse(false, waitingCount);
        }

        return new WaitOrderResponse(true, waitingCount);
    }

    /**
     * 사용자가 대기열에 존재하지 않는다면, 마지막에 추가합니다. 사용자가 대기열에 존재한다면 최근 요청 시각을 갱신합니다. 이후 사용자의 현재 대기열 순서를 반환합니다.
     *
     * @param ticketId
     * @param loginMemberId
     * @return 사용자의 대기열 순서
     */
    private Long getWaitOrderInternal(Long ticketId, Long loginMemberId) {
        Long waitingCount = waitingRepository.getWaitingCount(ticketId, loginMemberId);

        if (waitingCount == null) {
            waitingRepository.addWaiting(ticketId, loginMemberId);
        } else {
            waitingRepository.updateRecentRequestTime(ticketId, loginMemberId);
        }

        waitingCount = waitingRepository.getWaitingCount(ticketId, loginMemberId);
        return waitingCount;
    }
}
