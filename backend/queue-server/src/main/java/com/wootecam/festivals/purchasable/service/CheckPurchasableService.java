package com.wootecam.festivals.purchasable.service;

import com.wootecam.festivals.global.utils.TimeProvider;
import com.wootecam.festivals.global.utils.UuidProvider;
import com.wootecam.festivals.purchasable.dto.PurchasableResponse;
import com.wootecam.festivals.purchasable.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.purchase.repository.PurchaseSessionRedisRepository;
import com.wootecam.festivals.domain.purchase.repository.TicketStockRedisRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckPurchasableService {

    private final UuidProvider uuidProvider;
    private final TimeProvider timeProvider;
    private final TicketStockRedisRepository ticketStockRedisRepository;
    private final PurchaseSessionRedisRepository purchaseSessionRedisRepository;

//    @Value("{purchasable.queue.size}")
//    private Integer PURCHASABLE_QUEUE_SIZE;

    public WaitOrderResponse getWaitOrder(Long ticketId, Long loginMemberId, LocalDateTime now) {
        return new WaitOrderResponse(false, 1L);
    }

    public PurchasableResponse checkPurchasable(Long ticketId, Long loginMemberId, LocalDateTime now) {

        // waitOrder = 현재 대기 순서 조회
        // 현재 대기 순서가 PURCHASABLE_QUEUE_SIZE 초과라면
        // return 200 - false, waitOrder

        //Tx2
        // 현재 재고 > 0
        // false: 현재 재고가 남아있는지 확인하여 없으면 return 400 매진

        // validFirstPurchase(memberId, ticketId)
        // false : 첫 구매인지 확인하여 구매 내역이 있으면 return 400 이미 구매

        // 결제 세션 저장
        Long ticketStockId = ticketStockRedisRepository.popTicketStock(ticketId);

        String purchaseSessionId = uuidProvider.getUuid();
        purchaseSessionRedisRepository.addPurchaseSession(ticketId, loginMemberId, ticketStockId, purchaseSessionId, 5L);

        // redis 재고 차감

        // return: 302 redirect

        return new PurchasableResponse(false, purchaseSessionId);
    }

//    // 결제 권한 발급 및 재고 차감
//    public void grantPaymentPermission(User user, Long ticketId) {
//        // 티켓 재고 차감
//        ticketService.reduceStock(ticketId);
//
//        // 결제 세션 생성
//        String sessionId = UUID.randomUUID().toString();
//        PaymentSession paymentSession = new PaymentSession(user.getId(), ticketId, LocalDateTime.now().plusMinutes(5));
//        redisTemplate.opsForValue().set("payment_session:" + sessionId, paymentSession, 5, TimeUnit.MINUTES);
//
//        // 사용자에게 결제 서버로 이동하도록 URL 제공
//        String paymentUrl = "https://payment.example.com/session/" + sessionId;
//        redirectUserToPaymentServer(paymentUrl);
//    }
}
