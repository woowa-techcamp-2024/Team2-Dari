package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.domain.purchase.PurchasableResponse;
import com.wootecam.festivals.domain.purchase.repository.PurchaseSessionRedisRepository;
import com.wootecam.festivals.domain.purchase.repository.PurchasedMemberRedisRepository;
import com.wootecam.festivals.domain.purchase.repository.TicketStockRedisRepository;
import com.wootecam.festivals.domain.purchase.repository.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.wait.exception.WaitErrorCode;
import com.wootecam.festivals.domain.wait.repository.WaitingRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.utils.UuidProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckPurchasableService {

    private final UuidProvider uuidProvider;

    private final WaitingRepository waitingRepository;
    private final TicketStockRedisRepository ticketStockRedisRepository;
    private final PurchaseSessionRedisRepository purchaseSessionRedisRepository;
    private final PurchasedMemberRedisRepository purchasedMemberRedisRepository;

    @Value("${purchasable.queue.size}")
    private Integer PURCHASABLE_QUEUE_SIZE;

    public PurchasableResponse checkPurchasable(Long ticketId, Long loginMemberId, LocalDateTime now) {
        Long waitingCount = waitingRepository.getWaitingCount(ticketId, loginMemberId);
        if (waitingCount == null || waitingCount > PURCHASABLE_QUEUE_SIZE) {
            log.warn("티켓 구매 불가 - 유효하지 않은 대기열 순서 - 티켓 ID: {}, 유저 ID: {}, 대기열 순서: {}", ticketId, loginMemberId,
                    waitingCount);
            throw new ApiException(WaitErrorCode.INVALID_WAIT_ORDER);
        }

        if (purchasedMemberRedisRepository.isPurchasedMember(loginMemberId, ticketId)) {
            log.warn("티켓 구매 불가 - 이미 구매한 티켓 ID: {}, 유저 ID: {}", ticketId, loginMemberId);
            throw new ApiException(PurchaseErrorCode.ALREADY_PURCHASED_TICKET);
        }

        //Tx2
        // 현재 재고 > 0
        // false: 현재 재고가 남아있는지 확인하여 없으면 return 400 매진

        // 결제 세션 저장
        String purchaseSessionId = uuidProvider.getUuid();
        purchaseSessionRedisRepository.addPurchaseSession(ticketId, loginMemberId, purchaseSessionId, 5L);

        // redis 재고 차감

        return new PurchasableResponse(false, purchaseSessionId);
    }
}
