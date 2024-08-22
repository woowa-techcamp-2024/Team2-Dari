package com.wootecam.festivals.domain.ticket.dto;

import com.wootecam.festivals.domain.ticket.entity.Ticket;
import java.time.LocalDateTime;

public record CachedTicketInfo(Long id,
                               String name,
                               LocalDateTime startSaleTime,
                               LocalDateTime endSaleTime,
                               Long price,
                               int quantity) {
    public static CachedTicketInfo from(Ticket ticket) {
        return new CachedTicketInfo(ticket.getId(),
                ticket.getName(),
                ticket.getStartSaleTime(),
                ticket.getEndSaleTime(),
                ticket.getPrice(),
                ticket.getQuantity());
    }
}