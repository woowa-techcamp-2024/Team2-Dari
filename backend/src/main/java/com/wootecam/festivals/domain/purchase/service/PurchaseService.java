package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.exception.MemberErrorCode;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.dto.PurchaseIdResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchasePreviewInfoResponse;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
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

    private final PurchaseRepository purchaseRepository;
    private final TicketStockRepository ticketStockRepository;
    private final MemberRepository memberRepository;
    private final TicketRepository ticketRepository;

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
        Ticket ticket = findTicketById(ticketId);
        validTicketPurchasableTime(now, ticket);

        Member member = memberRepository.getReferenceById(loginMemberId);
        validFirstTicketPurchase(ticket, member);

        TicketStock ticketStock = getTicketStockForUpdate(ticket);
        decreaseStock(ticketStock);
        ticketStockRepository.flush(); // 재고 차감 쿼리를 먼저 실행하기 위한 flush
        Purchase newPurchase = purchaseRepository.save(ticket.createPurchase(member));

        log.debug("티켓 구매 완료 - 티켓 ID: {}, 회원 ID: {}, 구매 ID: {}", ticketId, loginMemberId, newPurchase.getId());
        return new PurchaseIdResponse(newPurchase.getId());
    }

    /**
     * 티켓 결제창에서 보여줄 정보 조회 (결제 창은 구매 버튼을 누르고 난 후 보이는 화면)
     *
     * @param memberId
     * @param festivalId
     * @param ticketId
     * @return PurchasePreviewInfoResponse
     */
    public PurchasePreviewInfoResponse getPurchasePreviewInfo(Long memberId, Long festivalId, Long ticketId) {
        Ticket ticket = findTicketByIdAndFestivalId(ticketId, festivalId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("회원을 찾을 수 없음 - 회원 ID: {}", memberId);
                    return new ApiException(MemberErrorCode.USER_NOT_FOUND);
                });
        TicketStock ticketStock = ticketStockRepository.findByTicket(ticket)
                .orElseThrow(() -> {
                    log.warn("티켓 재고를 찾을 수 없음 - 티켓 ID: {}", ticket.getId());
                    return new ApiException(TicketErrorCode.TICKET_STOCK_NOT_FOUND);
                });

        if (ticketStock.getRemainStock() == 0) {
            log.warn("티켓 재고 부족 - 티켓 ID: {}", ticket.getId());
            throw new ApiException(TicketErrorCode.TICKET_STOCK_EMPTY);
        }

        validTicketPurchasableTime(LocalDateTime.now(), ticket);
        validFirstTicketPurchase(ticket, member);

        return new PurchasePreviewInfoResponse(festivalId, ticket.getFestival().getTitle(),
                ticket.getFestival().getFestivalImg(),
                ticket.getId(), ticket.getName(), ticket.getDetail(), ticket.getPrice(), ticket.getQuantity(),
                ticketStock.getRemainStock(),
                ticket.getEndSaleTime());
    }

    private TicketStock getTicketStockForUpdate(Ticket ticket) {
        return ticketStockRepository.findByTicketForUpdate(ticket)
                .orElseThrow(() -> {
                    log.warn("티켓 재고를 찾을 수 없음 - 티켓 ID: {}", ticket.getId());
                    return new ApiException(TicketErrorCode.TICKET_STOCK_NOT_FOUND);
                });
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

    private Ticket findTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.warn("티켓을 찾을 수 없습니다. 티켓 ID: {}", ticketId);
                    return new ApiException(TicketErrorCode.TICKET_NOT_FOUND);
                });
    }

    // 페스티벌 ID와 티켓 ID로 티켓을 찾는 메소드, join fetch를 사용하여 티켓과 페스티벌을 한번에 가져옴
    private Ticket findTicketByIdAndFestivalId(Long ticketId, Long festivalId) {
        return ticketRepository.findByIdAndFestivalId(ticketId, festivalId)
                .orElseThrow(() -> {
                    log.warn("티켓 또는 페스티벌을 찾을 수 없습니다. 티켓 ID: {}, 축제 ID: {}", ticketId, festivalId);
                    return new ApiException(TicketErrorCode.TICKET_NOT_FOUND);
                });
    }
}
