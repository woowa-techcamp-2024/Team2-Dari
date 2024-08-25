package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.domain.payment.excpetion.PaymentErrorCode;
import com.wootecam.festivals.domain.payment.service.PaymentService;
import com.wootecam.festivals.domain.payment.service.PaymentService.PaymentStatus;
import com.wootecam.festivals.domain.purchase.repository.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.ticket.dto.CachedTicketInfo;
import com.wootecam.festivals.domain.ticket.service.TicketCacheService;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.service.QueueService;
import com.wootecam.festivals.global.utils.TimeProvider;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseFacadeService {

    private final PaymentService paymentService;
    private final TicketCacheService ticketCacheService;
    private final QueueService queueService;
    private final TicketStockRollbacker ticketReserveCanceler;
    private final TimeProvider timeProvider;

    private final Map<String, PurchaseData> pendingPurchases = new ConcurrentHashMap<>();

    public String processPurchase(PurchaseData purchaseData) {
        validatePurchase(purchaseData);

        String paymentId = paymentService.initiatePayment(purchaseData.memberId(), purchaseData.ticketId());
        pendingPurchases.put(paymentId, purchaseData);

        return paymentId;
    }

    public PaymentService.PaymentStatus getPaymentStatus(String paymentId) {
        PaymentStatus paymentStatus = paymentService.getPaymentStatus(paymentId);
        if (paymentStatus == null) {
            throw new ApiException(PaymentErrorCode.PAYMENT_NOT_EXIST);
        }
        return paymentStatus;
    }

    @Scheduled(fixedRate = 500) // 0.5초마다 실행
    @Transactional
    public void processPaymentResults() {
        pendingPurchases.forEach((paymentId, purchaseData) -> {
            PaymentService.PaymentStatus status = paymentService.getPaymentStatus(paymentId);
            if (status != PaymentService.PaymentStatus.PENDING) {
                handlePaymentResult(paymentId, status, purchaseData);
            }
        });
    }

    private void handlePaymentResult(String paymentId, PaymentService.PaymentStatus status, PurchaseData purchaseData) {
        switch (status) {
            case SUCCESS:
                queueService.addPurchase(purchaseData);
                pendingPurchases.remove(paymentId);
                break;
            case FAILED:
                ticketReserveCanceler.rollbackTicketStock(purchaseData.ticketStockId());
                pendingPurchases.remove(paymentId);
                break;
            case PENDING:
                break;
        }
    }

    private void validatePurchase(PurchaseData purchaseData) {
        // 추후 검증 로직 추가될 가능성을 고려해 메서드 생성
        if (!isTicketAvailableForPurchase(purchaseData.ticketId())) {
            throw new ApiException(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME);
        }
    }

    private boolean isTicketAvailableForPurchase(Long ticketId) {
        CachedTicketInfo ticketInfo = ticketCacheService.getTicketInfo(ticketId); // 없다면 내부에서 db조회 후 가져온다.

        LocalDateTime now = timeProvider.getCurrentTime();
        return now.isAfter(ticketInfo.startSaleTime()) && now.isBefore(ticketInfo.endSaleTime());
    }
}
