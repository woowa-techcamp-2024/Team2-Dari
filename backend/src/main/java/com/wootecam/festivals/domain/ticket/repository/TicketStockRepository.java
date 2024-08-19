package com.wootecam.festivals.domain.ticket.repository;

import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TicketStockRepository extends JpaRepository<TicketStock, Long> {

    @Query("SELECT ts FROM TicketStock ts WHERE ts.ticket = :ticket")
    Optional<TicketStock> findByTicket(Ticket ticket);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ts FROM TicketStock ts WHERE ts.ticket = :ticket")
    Optional<TicketStock> findByTicketForUpdate(Ticket ticket);
}