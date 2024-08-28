package com.wootecam.festivals.domain.purchase.repository;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.my.dto.MyPurchasedFestivalResponse;
import com.wootecam.festivals.domain.my.dto.MyPurchasedTicketResponse;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsByTicketAndMember(Ticket ticket, Member member);

    @Query("""
            SELECT new com.wootecam.festivals.domain.my.dto.MyPurchasedTicketResponse(
                p.id, p.purchaseTime, p.purchaseStatus,  
                new com.wootecam.festivals.domain.ticket.dto.TicketWithoutStockResponse( 
                    t.id, t.name, t.detail, t.price, t.quantity,
                    t.startSaleTime, t.endSaleTime, t.refundEndTime, t.createdAt, t.updatedAt),
                new com.wootecam.festivals.domain.festival.dto.FestivalResponse(
                    f.id, f.admin.id, f.title, f.description, f.festivalImg, f.startTime, f.endTime, f.festivalPublicationStatus, f.festivalProgressStatus),
                c.id,
                c.isChecked,
                c.checkinTime
            )
            FROM Purchase p 
            JOIN p.ticket t ON t.id = p.ticket.id
            JOIN t.festival f ON f.id = t.festival.id
            LEFT JOIN Checkin c ON c.member.id = p.member.id AND c.ticket.id = t.id
            WHERE p.member.id = :memberId AND t.id = :ticketId
            """)
    Optional<MyPurchasedTicketResponse> findByMemberIdAndTicketId(Long memberId, Long ticketId); // TicketStock 조인 x

    @Query("""
                SELECT new com.wootecam.festivals.domain.my.dto.MyPurchasedFestivalResponse(
                    f.title, f.festivalImg, f.startTime, f.endTime, f.festivalPublicationStatus, f.festivalProgressStatus,
                    new com.wootecam.festivals.domain.festival.dto.FestivalAdminResponse(
                        a.id, a.name, a.email, a.profileImg
                    ),
                    p.id, p.purchaseTime, t.id
                )
                FROM Purchase p
                JOIN p.ticket t
                JOIN t.festival f
                JOIN f.admin a
                WHERE p.member.id = :memberId
                AND f.isDeleted = false
                AND (p.purchaseTime < :purchaseTime OR (p.purchaseTime = :purchaseTime AND p.id < :purchaseId))
                ORDER BY p.purchaseTime DESC, p.id DESC
            """)
    List<MyPurchasedFestivalResponse> findPurchasedFestivalsCursorOrderPurchaseTimeDesc(
            @Param("memberId") Long memberId,
            @Param("purchaseTime") LocalDateTime purchaseTime,
            @Param("purchaseId") Long purchaseId,
            Pageable pageable);
}