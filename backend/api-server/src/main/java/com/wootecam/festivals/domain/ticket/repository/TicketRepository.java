package com.wootecam.festivals.domain.ticket.repository;

import com.wootecam.festivals.domain.ticket.dto.TicketResponse;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t WHERE t.id = :id AND t.isDeleted = false")
    @Override
    Optional<Ticket> findById(Long id);

    @Query("""
            SELECT new com.wootecam.festivals.domain.ticket.dto.TicketResponse(
                t.id, t.name, t.detail, t.price, t.quantity, 
                (SELECT count(ts.id) FROM TicketStock ts WHERE ts.ticket.id = t.id AND ts.memberId IS NULL),
                t.startSaleTime, t.endSaleTime, t.refundEndTime, t.createdAt, t.updatedAt
            ) 
            FROM Ticket t 
            WHERE t.festival.id = :festivalId AND t.isDeleted = false
            """)
    List<TicketResponse> findTicketsByFestivalIdWithRemainStock(Long festivalId);

    @Query("SELECT t FROM Ticket t join fetch t.festival WHERE t.id = :ticketId AND t.festival.id = :festivalId AND t.isDeleted = false")
    Optional<Ticket> findByIdAndFestivalId(Long ticketId, Long festivalId);

    @Query("""
        SELECT new com.wootecam.festivals.domain.ticket.dto.TicketResponse(
                t.id, t.name, t.detail, t.price, t.quantity, 
                (SELECT count(ts.id) FROM TicketStock ts WHERE ts.ticket.id = t.id AND ts.memberId IS NULL),
                t.startSaleTime, t.endSaleTime, t.refundEndTime, t.createdAt, t.updatedAt
            ) 
            FROM Ticket t 
            WHERE t.startSaleTime BETWEEN :startTime AND :endTime 
            AND t.isDeleted = false
    """)
    List<TicketResponse> findTicketsByStartSaleTimeBetweenRangeWithRemainStock(LocalDateTime startTime, LocalDateTime endTime);

    @Query("""
            SELECT new com.wootecam.festivals.domain.ticket.dto.TicketResponse(
                t.id, t.name, t.detail, t.price, t.quantity, 
                (SELECT count(ts.id) FROM TicketStock ts WHERE ts.ticket.id = t.id AND ts.memberId IS NULL),
                t.startSaleTime, t.endSaleTime, t.refundEndTime, t.createdAt, t.updatedAt
            ) 
            FROM Ticket t 
            WHERE t.startSaleTime <= :now AND t.endSaleTime >= :now
            AND t.isDeleted = false
    """)
    List<TicketResponse> findSaleOngoingTicketsWithRemainStock(LocalDateTime now);
}
