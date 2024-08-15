package com.wootecam.festivals.domain.ticket.utils;

import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketFinder {

    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;

    public Ticket findTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ApiException(TicketErrorCode.TICKET_NOT_FOUND));
    }

    public TicketStock findTicketStockByTicket(Ticket ticket) {
        return ticketStockRepository.findByTicket(ticket)
                .orElseThrow(() -> new ApiException(TicketErrorCode.TICKET_STOCK_NOT_FOUND));
    }
}
