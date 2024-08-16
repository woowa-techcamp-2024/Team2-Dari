package com.wootecam.festivals.domain.purchase.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final TicketFinder ticketFinder;
    private final PurchaseRepository purchaseRepository;
    private final TicketStockRepository ticketStockRepository;
    private final MemberRepository memberRepository;

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

        return new PurchaseIdResponse(newPurchase.getId());
    }

    private void decreaseStock(TicketStock ticketStock) {
        try {
            ticketStock.decreaseStock();
        } catch (IllegalStateException e) {
            throw new ApiException(TicketErrorCode.TICKET_STOCK_EMPTY);
        }
    }

    private void validTicketPurchasableTime(LocalDateTime now, Ticket ticket) {
        if (now.isBefore(ticket.getStartSaleTime())
                || now.isAfter(ticket.getEndSaleTime())) {
            throw new ApiException(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME);
        }
    }

    private void validFirstTicketPurchase(Ticket ticket, Member member) {
        if (purchaseRepository.existsByTicketAndMember(ticket, member)) {
            throw new ApiException(PurchaseErrorCode.ALREADY_PURCHASED_TICKET);
        }
    }
}
