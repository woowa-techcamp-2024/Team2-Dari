package com.wootecam.festivals.domain.ticket.repository;

import com.wootecam.festivals.domain.ticket.entity.Ticket;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("select t from Ticket t where t.id = :ticketId and t.isDeleted = false")
    @Override
    Optional<Ticket> findById(Long ticketId);
}
