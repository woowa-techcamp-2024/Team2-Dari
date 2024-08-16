package com.wootecam.festivals.domain.ticket.repository;

import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketStockRepository extends JpaRepository<TicketStock, Long> {
}