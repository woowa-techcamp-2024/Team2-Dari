package com.wootecam.festivals.domain.ticket.repository;

import com.wootecam.festivals.domain.ticket.dto.TicketResponse;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t WHERE t.id = :ticketId AND t.isDeleted = false")
    @Override
    Optional<Ticket> findById(Long ticketId);

    @Query("""
            SELECT new com.wootecam.festivals.domain.ticket.dto.TicketResponse(
                t.id, t.name, t.detail, t.price, t.quantity, ts.remainStock, 
                t.startSaleTime, t.endSaleTime, t.refundEndTime, t.createdAt, t.updatedAt
            ) 
            FROM Ticket t 
            INNER JOIN TicketStock ts ON t.id = ts.ticket.id 
            WHERE t.festival.id = :festivalId AND t.isDeleted = false
            """)
    List<TicketResponse> findTicketsByFestivalIdWithRemainStock(Long festivalId);
}
