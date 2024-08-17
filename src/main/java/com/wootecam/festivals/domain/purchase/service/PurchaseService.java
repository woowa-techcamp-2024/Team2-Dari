package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.domain.checkin.repository.CheckinRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.dto.PurchaseIdResponse;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.domain.ticket.utils.TicketFinder;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 티켓 구매 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final TicketFinder ticketFinder;
    private final PurchaseRepository purchaseRepository;
    private final TicketStockRepository ticketStockRepository;
    private final MemberRepository memberRepository;

    /**
     * 티켓 구매 처리
     *
     * @param ticketId      구매할 티켓 ID
     * @param loginMemberId 구매자 ID
     * @param now           현재 시간
     * @return 생성된 구매 내역 ID
     */
    @Transactional
    public PurchaseIdResponse createPurchase(Long ticketId, Long loginMemberId, LocalDateTime now) {
        Ticket ticket = ticketFinder.findTicketById(ticketId);
        validTicketPurchasableTime(now, ticket);

        Member member = memberRepository.getReferenceById(loginMemberId);
        validFirstTicketPurchase(ticket, member);

        TicketStock ticketStock = ticketStockRepository.findByTicketForUpdate(ticket)
                .orElseThrow(() -> new ApiException(TicketErrorCode.TICKET_STOCK_NOT_FOUND));
        decreaseStock(ticketStock);
        ticketStockRepository.flush(); // 재고 차감 쿼리를 먼저 실행하기 위한 flush
        Purchase newPurchase = purchaseRepository.save(ticket.createPurchase(member));

        log.debug("티켓 구매 완료 - 티켓 ID: {}, 회원 ID: {}, 구매 ID: {}", ticketId, loginMemberId, newPurchase.getId());
        return new PurchaseIdResponse(newPurchase.getId());
    }

    private void decreaseStock(TicketStock ticketStock) {
        try {
            ticketStock.decreaseStock();
        } catch (IllegalStateException e) {
            log.warn("티켓 재고 부족 - 티켓 ID: {}", ticketStock.getTicket().getId());
            throw new ApiException(TicketErrorCode.TICKET_STOCK_EMPTY);
        }
    }

    private void validTicketPurchasableTime(LocalDateTime now, Ticket ticket) {
        if (now.isBefore(ticket.getStartSaleTime()) || now.isAfter(ticket.getEndSaleTime())) {
            log.warn("유효하지 않은 티켓 구매 시간 - 티켓 ID: {}, 현재 시간: {}", ticket.getId(), now);
            throw new ApiException(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME);
        }
    }

    private void validFirstTicketPurchase(Ticket ticket, Member member) {
        if (purchaseRepository.existsByTicketAndMember(ticket, member)) {
            log.warn("이미 구매한 티켓 - 티켓 ID: {}, 회원 ID: {}", ticket.getId(), member.getId());
            throw new ApiException(PurchaseErrorCode.ALREADY_PURCHASED_TICKET);
        }
    }
}