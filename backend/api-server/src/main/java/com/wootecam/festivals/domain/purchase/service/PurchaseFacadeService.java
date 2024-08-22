package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.domain.checkin.dto.CheckinIdResponse;
import com.wootecam.festivals.domain.checkin.service.CheckinService;
import com.wootecam.festivals.domain.payment.excpetion.PaymentErrorCode;
import com.wootecam.festivals.domain.payment.service.PaymentService;
import com.wootecam.festivals.domain.payment.service.PaymentService.PaymentStatus;
import com.wootecam.festivals.domain.purchase.dto.PurchaseIdResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchaseTicketResponse;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.ticket.dto.CachedTicketInfo;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
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
    private final TicketStockRepository ticketStockRepository;
    private final TimeProvider timeProvider;

    // 삭제 예정
    private final PurchaseService purchaseService;
    private final CheckinService checkinService;

    private final Map<String, PurchaseData> pendingPurchases = new ConcurrentHashMap<>();

    // 삭제 예정
    @Deprecated
    @Transactional
    public PurchaseTicketResponse purchaseTicket(Long memberId, Long festivalId, Long ticketId) {
        log.debug("티켓 구매 요청 - 축제 ID: {}, 티켓 ID: {}, 회원 ID: {}", festivalId, ticketId, memberId);
        PurchaseIdResponse purchaseResponse = purchaseService.createPurchase(ticketId, memberId, LocalDateTime.now());
        log.debug("티켓 구매 완료 - 구매 ID: {}", purchaseResponse.purchaseId());

        paymentService.pay(memberId, ticketId);

        log.debug("체크인 정보 생성 요청 - 티켓 ID: {}, 회원 ID: {}", ticketId, memberId);
        CheckinIdResponse checkinResponse = checkinService.createPendingCheckin(memberId, ticketId);
        log.debug("체크인 정보 생성 완료 - 체크인 ID {}", checkinResponse);

        return new PurchaseTicketResponse(purchaseResponse.purchaseId(), checkinResponse.checkinId());
    }

    @Transactional
    public String processPurchase(PurchaseData purchaseData) {
        validatePurchase(purchaseData);
        int updated = ticketStockRepository.decreaseStockAtomically(purchaseData.ticketId());
        if (updated <= 0) {
            throw new ApiException(TicketErrorCode.TICKET_STOCK_EMPTY);
        }

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

    @Scheduled(fixedRate = 5000) // 5초마다 실행
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
                compensateStock(purchaseData.ticketId());
                pendingPurchases.remove(paymentId);
                break;
            case PENDING:
                break;
        }
    }

    private void compensateStock(Long ticketId) {
        ticketStockRepository.increaseStockAtomically(ticketId);
    }

    private void validatePurchase(PurchaseData purchaseData) {
        // 추후 검증 로직 추가될 가능성을 고려해 메서드 생성
        if (!isTicketAvailableForPurchase(purchaseData.ticketId())) {
            throw new ApiException(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME);
        }
    }

    private boolean isTicketAvailableForPurchase(Long ticketId) {
        CachedTicketInfo ticketInfo = ticketCacheService.getTicketInfo(ticketId); // 없다면 내부에서 db조회 후 가져온다.

        // 도메인 로직 아닌가?
        LocalDateTime now = timeProvider.getCurrentTime();
        return now.isAfter(ticketInfo.startSaleTime()) && now.isBefore(ticketInfo.endSaleTime());
    }
}
