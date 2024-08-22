package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.domain.checkin.dto.CheckinIdResponse;
import com.wootecam.festivals.domain.checkin.service.CheckinService;
import com.wootecam.festivals.domain.purchase.dto.PurchaseIdResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchaseTicketResponse;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseFacadeService {

    private final PurchaseService purchaseService;
    private final CheckinService checkinService;
    private final PaymentService paymentService;

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
}
