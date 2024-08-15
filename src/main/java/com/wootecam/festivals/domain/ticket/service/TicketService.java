package com.wootecam.festivals.domain.ticket.service;


import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.dto.TicketIdResponse;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;
    private final FestivalRepository festivalRepository;

    @Transactional
    public TicketIdResponse createTicket(Long festivalId, TicketCreateRequest request) {
        Ticket newTicket = saveTicket(festivalId, request);
        saveTicketStock(newTicket);

        return new TicketIdResponse(newTicket.getId());
    }

    private Ticket saveTicket(Long festivalId, TicketCreateRequest request) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new ApiException(FestivalErrorCode.FestivalNotFoundException));

        return ticketRepository.save(request.toEntity(festival));
    }

    private void saveTicketStock(Ticket newTicket) {
        TicketStock newTicketStock = TicketStock.builder()
                .remainStock(newTicket.getQuantity())
                .ticket(newTicket)
                .build();
        ticketStockRepository.save(newTicketStock);
    }
}
