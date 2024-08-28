package com.wootecam.festivals.domain.wait.service;

import com.wootecam.festivals.domain.purchase.repository.TicketStockCountRedisRepository;
import com.wootecam.festivals.domain.wait.PassOrder;
import com.wootecam.festivals.domain.wait.dto.WaitOrderCreateResponse;
import com.wootecam.festivals.domain.wait.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.exception.WaitErrorCode;
import com.wootecam.festivals.domain.wait.repository.WaitingRedisRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
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
    private final PassOrder passOrder;

    @Value("${wait.queue.pass-chunk-size-divider}")
    private Integer passChunkSize;

    /**
     * 사용자를 대기열에 추가하고, 대기열 블록 순서를 발급합니다.
     *
     * @param ticketId
     * @param loginMemberId
     * @return
     */
    public WaitOrderCreateResponse createWaitOrder(Long ticketId, Long loginMemberId) {
        Boolean isWaiting = waitingRepository.exists(ticketId, loginMemberId);
        if (isWaiting) {
            throw new ApiException(WaitErrorCode.ALREADY_WAITING);
        }

        waitingRepository.addWaiting(ticketId, loginMemberId);

        Long size = waitingRepository.getSize(ticketId);
        Long newWaitOrderBlockNum = size / passChunkSize + 1;
        log.debug("대기열 블록 순서 발급 - {}", newWaitOrderBlockNum);

        return new WaitOrderCreateResponse(newWaitOrderBlockNum);
    }

    /**
     * 사용자가 구매 페이지로 진입할 수 있는지를 사용자 입장 순서와 현재 입장 순서로 판단합니다. 사용자 입장 순서가 현재 입장 순서와 같고, 재고가 남았다면 재고를 차감하고, 구매 페이지로 진입할 수
     * 있습니다.
     *
     * @param ticketId
     * @param loginMemberId
     * @param waitOrderBlock
     * @return 사용자가 구매 페이지로 진입할 수 있는지 여부, 대기열 순서
     */
    public WaitOrderResponse getWaitOrder(Long ticketId, Long loginMemberId, Long waitOrderBlock) {
        Boolean isWaiting = waitingRepository.exists(ticketId, loginMemberId);
        if (!isWaiting) {
            throw new ApiException(WaitErrorCode.CANNOT_FOUND_USER);
        }

        if (waitOrderBlock == null || waitOrderBlock < 0) {
            throw new ApiException(WaitErrorCode.INVALID_WAIT_ORDER);
        }

        Long currentPassOrder = passOrder.get(ticketId);
        if (waitOrderBlock < currentPassOrder - 1) {
            throw new ApiException(WaitErrorCode.QUEUE_EXITED);
        }

        if (ticketStockCountRedisRepository.getTicketStockCount(ticketId) <= 0) {
            throw new ApiException(WaitErrorCode.NO_STOCK);
        }

        Long waitOrder = (waitOrderBlock - passOrder.get(ticketId)) * passChunkSize;
        // 사용자 입장 순서가 입장 순서 갱신 직전 발급되고, 사용자 입장 순서가 갱신될 수 있으므로 1 차이까지 허용
        if ((waitOrderBlock.equals(currentPassOrder) || waitOrderBlock.equals(currentPassOrder - 1)) &&
                ticketStockCountRedisRepository.checkAndDecreaseStock(ticketId, loginMemberId)) {
            return new WaitOrderResponse(true, waitOrder);
        }

        return new WaitOrderResponse(false, waitOrder);
    }

    @Scheduled(fixedRate = 5000)
    public void updateCurrentPassOrder() {
        Long ticketId = 1L;

        Long waitSize = waitingRepository.getSize(ticketId);
        long currentWaitOrder = waitSize / passChunkSize + 1;
        if (currentWaitOrder < 0) {
            throw new IllegalArgumentException("Invalid currentWaitOrder value: " + currentWaitOrder);
        }

        passOrder.updateByWaitOrder(ticketId, currentWaitOrder);
    }
}
