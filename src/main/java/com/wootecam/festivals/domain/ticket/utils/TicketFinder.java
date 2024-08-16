package com.wootecam.festivals.domain.ticket.utils;

import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TicketFinder는 Ticket과 관련된 정보를 찾고, 없는 경우 예외를 발생시킵니다.
 */
@Component
@RequiredArgsConstructor
public class TicketFinder {

    private final TicketRepository ticketRepository;

    public Ticket findTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ApiException(TicketErrorCode.TICKET_NOT_FOUND));
    }
}
