package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.domain.payment.service.PaymentService;
import com.wootecam.festivals.domain.payment.service.PaymentService.PaymentStatus;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.service.TicketCacheService;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.service.QueueService;
import com.wootecam.festivals.global.utils.TimeProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseFacadeService {

    private final PaymentService paymentService;
    private final TicketCacheService ticketCacheService;
    private final QueueService queueService;
    private final TimeProvider timeProvider;

    public String processPurchase(PurchaseData purchaseData) {
        validatePurchase(purchaseData);
        String paymentId = UUID.randomUUID().toString();

        paymentService.initiatePayment(paymentId, purchaseData.memberId(), purchaseData.ticketId())
                .thenAcceptAsync(status -> handlePaymentResult(paymentId, status, purchaseData));
        return paymentId;
    }

    public PaymentService.PaymentStatus getPaymentStatus(String paymentId) {
        return paymentService.getPaymentStatus(paymentId);
    }

    private void handlePaymentResult(String paymentId, PaymentStatus status, PurchaseData purchaseData) {
        switch (status) {
            case SUCCESS:
                queueService.addPurchase(purchaseData);
                break;
            case FAILED:
                break;
            case PENDING:
                break;
        }
        paymentService.updatePaymentStatus(paymentId, status);
    }

    private void validatePurchase(PurchaseData purchaseData) {
        // 추후 검증 로직 추가될 가능성을 고려해 메서드 생성
        if (!isTicketAvailableForPurchase(purchaseData.ticketId())) {
            throw new ApiException(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME);
        }
    }

    private boolean isTicketAvailableForPurchase(Long ticketId) {
        Ticket ticket = ticketCacheService.getTicket(ticketId); // 없다면 내부에서 db조회 후 가져온다.

        LocalDateTime now = timeProvider.getCurrentTime();
        return now.isAfter(ticket.getStartSaleTime()) && now.isBefore(ticket.getEndSaleTime());
    }
}
