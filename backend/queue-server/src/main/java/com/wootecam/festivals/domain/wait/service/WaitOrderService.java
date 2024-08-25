package com.wootecam.festivals.domain.wait.service;

import com.wootecam.festivals.domain.wait.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitOrderService {

    private final WaitingRepository waitingRepository;

    @Value("${purchasable.queue.size}")
    private Integer PURCHASABLE_QUEUE_SIZE;

    /**
     * 사용자의 대기열 순서를 조회하고, 대기열에 존재하지 않는다면 마지막에 추가합니다.
     *
     * @param ticketId
     * @param loginMemberId
     * @return
     */
    public WaitOrderResponse getWaitOrder(Long ticketId, Long loginMemberId) {
        Long waitingCount = getWaitOrderInternal(ticketId, loginMemberId);

        if (waitingCount > PURCHASABLE_QUEUE_SIZE) {
            return new WaitOrderResponse(false, waitingCount);
        }

        waitingRepository.removeWaiting(ticketId, loginMemberId);
        return new WaitOrderResponse(true, waitingCount);
    }

    /**
     * 사용자가 대기열에 존재하지 않는다면, 마지막에 추가합니다. 사용자가 대기열에 존재한다면 TTL을 늘려줍니다. 사용자의 현재 대기열 순서를 반환합니다.
     *
     * @param ticketId
     * @param loginMemberId
     * @return 사용자의 대기열 순서
     */
    private Long getWaitOrderInternal(Long ticketId, Long loginMemberId) {
        Long waitingCount = waitingRepository.getWaitingCount(ticketId, loginMemberId);
        if (waitingCount == null) {
            waitingRepository.addWaiting(ticketId, loginMemberId, 5L);
        } else {
            waitingRepository.extendWaiting(ticketId, loginMemberId, 5L);
        }
        waitingCount = waitingRepository.getWaitingCount(ticketId, loginMemberId);
        return waitingCount;
    }
}
