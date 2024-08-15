package com.wootecam.festivals.domain.ticket.service;


import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.dto.TicketIdResponse;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final FestivalRepository festivalRepository;

    public TicketIdResponse createTicket(Long festivalId, TicketCreateRequest request) {

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new ApiException(FestivalErrorCode.FestivalNotFoundException));

        return new TicketIdResponse(ticketRepository
                .save(request.toEntity(festival))
                .getId());
    }
}
