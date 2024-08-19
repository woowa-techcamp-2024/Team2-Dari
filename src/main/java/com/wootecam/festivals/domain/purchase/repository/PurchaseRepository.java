package com.wootecam.festivals.domain.purchase.repository;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.my.dto.MyPurchasedTicketResponse;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsByTicketAndMember(Ticket ticket, Member member);

    @Query("""
            SELECT new com.wootecam.festivals.domain.my.dto.MyPurchasedTicketResponse(
            p.id, p.purchaseTime, p.purchaseStatus,  
            new com.wootecam.festivals.domain.ticket.dto.TicketWithoutStockResponse( 
            t.id, t.name, t.detail, t.price, t.quantity,
            t.startSaleTime, t.endSaleTime, t.refundEndTime, t.createdAt, t.updatedAt)
            , new com.wootecam.festivals.domain.festival.dto.FestivalResponse(
                    f.id, f.admin.id, f.title, f.description, f.festivalImg, f.startTime, f.endTime, f.festivalPublicationStatus, f.festivalProgressStatus
                ))
            FROM Purchase p 
            JOIN p.ticket t ON t.id = p.ticket.id
            JOIN t.festival f ON f.id = t.festival.id
            WHERE p.member.id = :memberId AND t.id = :ticketId
            """)
    Optional<MyPurchasedTicketResponse> findByMemberIdAndTicketId(Long memberId, Long ticketId); // TicketStock 조인 x
}