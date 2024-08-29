package com.wootecam.festivals.domain.wait.service;

import com.wootecam.festivals.domain.purchase.repository.TicketStockCountRedisRepository;
import com.wootecam.festivals.domain.ticket.entity.TicketInfo;
import com.wootecam.festivals.domain.ticket.repository.TicketInfoRedisRepository;
import com.wootecam.festivals.domain.wait.PassOrder;
import com.wootecam.festivals.domain.wait.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.exception.WaitErrorCode;
import com.wootecam.festivals.domain.wait.repository.PassOrderRedisRepository;
import com.wootecam.festivals.domain.wait.repository.WaitingRedisRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.utils.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitOrderService {

    private final WaitingRedisRepository waitingRepository;
    private final TicketStockCountRedisRepository ticketStockCountRedisRepository;
    private final PassOrderRedisRepository passOrderRedisRepository;
    private final TicketInfoRedisRepository ticketInfoRedisRepository;
    private final PassOrder passOrder;
    private final TimeProvider timeProvider;

    @Value("${wait.queue.pass-chunk-size}")
    private Long passChunkSize;

    /**
     * 사용자가 구매 페이지로 진입할 수 있는지를 사용자 대기 순서와 현재 입장 범위로 판단합니다.
     * 사용자 대기 순서가 현재 입장 범위에 포함되고, 재고가 남았다면 재고를 차감하고, 구매 페이지로 진입할 수
     * 있습니다.
     *
     * @param ticketId
     * @param loginMemberId
     * @param waitOrder
     * @return 사용자가 구매 페이지로 진입할 수 있는지 여부, 대기열 순서
     */
    public WaitOrderResponse getWaitOrder(Long ticketId, Long loginMemberId, Long waitOrder) {
        validTicketSaleTime(ticketId);

        Boolean isWaiting = waitingRepository.exists(ticketId, loginMemberId);

        Long curWaitOrder = waitOrder;
        validWaitOrderWithWaiter(curWaitOrder, isWaiting);

        // 대기열 참가 및 대기 순서 발급, 만약 현재 입장 순서 범위라면 대기열 통과
        Long currentPassOrder = getCurrentPassOrder(ticketId);
        if (!isWaiting && curWaitOrder == null) {
            return getNewWaitOrderForNewUser(ticketId, loginMemberId, currentPassOrder);
        }

        validStockRemains(ticketId);

        // 대기 순서가 현재 입장 순서 범위에 포함된다면 대기열 통과 가능
        if (canPass(curWaitOrder, currentPassOrder)) {
            ticketStockCountRedisRepository.checkAndDecreaseStock(ticketId);
            return new WaitOrderResponse(true, curWaitOrder - currentPassOrder, curWaitOrder);
        }

        // 대기 순서가 현재 입장 순서 범위의 최소값보다 작거나 같다면, 이탈 유저이므로 새로운 대기 순서 발급
        if (curWaitOrder <= curMinPassOrder(currentPassOrder)) {
            return getNewWaitOrderForExitedUser(ticketId, currentPassOrder);
        }

        // 대기가 현재 입장 순서 범위에 포함되지 않는다면 대기열 통과 불가
        return new WaitOrderResponse(false, curWaitOrder - currentPassOrder, curWaitOrder);
    }

    private WaitOrderResponse getNewWaitOrderForExitedUser(Long ticketId, Long currentPassOrder) {
        Long newWaitOrder = waitingRepository.getSize(ticketId);
        Long relativeWaitOrder = newWaitOrder - currentPassOrder;
        return new WaitOrderResponse(false, relativeWaitOrder, newWaitOrder);
    }

    private WaitOrderResponse getNewWaitOrderForNewUser(Long ticketId, Long loginMemberId, Long currentPassOrder) {
        Long curWaitOrder;
        curWaitOrder = joinWaitOrder(ticketId, loginMemberId);
        validStockRemains(ticketId);
        if (canPass(curWaitOrder, currentPassOrder)) {
            return new WaitOrderResponse(true, curWaitOrder - currentPassOrder, curWaitOrder);
        } else {
            return new WaitOrderResponse(false, curWaitOrder - currentPassOrder, curWaitOrder);
        }
    }

    private Long getCurrentPassOrder(Long ticketId) {
        return passOrder.get(ticketId);
    }

    // 티켓 판매 시간이 아닌 경우 예외 반환
    private void validTicketSaleTime(Long ticketId) {
        TicketInfo ticketInfo = ticketInfoRedisRepository.getTicketInfo(ticketId);
        if (ticketInfo == null) {
            throw new ApiException(WaitErrorCode.INVALID_TICKET);
        }

        if (ticketInfo.isNotOnSale(timeProvider.getCurrentTime())) {
            throw new ApiException(WaitErrorCode.NOT_ON_SALE);
        }
    }

    private long curMinPassOrder(Long currentPassOrder) {
        return currentPassOrder - passChunkSize;
    }

    // 재고가 없는 경우 예외 반환
    private void validStockRemains(Long ticketId) {
        if (ticketStockCountRedisRepository.getTicketStockCount(ticketId) <= 0) {
            throw new ApiException(WaitErrorCode.NO_STOCK);
        }
    }

    // 대기열의 사용자가 대기열 번호를 보내지 않은 경우 예외 반환
    private void validWaitOrderWithWaiter(Long waitOrder, Boolean isWaiting) {
        if (isWaiting && (waitOrder == null || waitOrder < 0)) {
            throw new ApiException(WaitErrorCode.INVALID_WAIT_ORDER);
        }
    }

    private boolean canPass(Long waitOrder, Long currentPassOrder) {
        return curMinPassOrder(currentPassOrder) < waitOrder && waitOrder <= currentPassOrder + passChunkSize;
    }

    private Long joinWaitOrder(Long ticketId, Long userId) {
        waitingRepository.addWaiting(ticketId, userId);
        return waitingRepository.getSize(ticketId);
    }

    @Scheduled(fixedRate = 5000)
    public void updateCurrentPassOrder() {
        Long ticketId = 1L;

        Long waitSize = waitingRepository.getSize(ticketId);
        Long newOrder = passOrderRedisRepository.increase(ticketId, passChunkSize, waitSize);
        passOrder.set(ticketId, newOrder);
    }
}
