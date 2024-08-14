package com.wootecam.festivals.domain.ticket.controller;

import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.dto.TicketIdResponse;
import com.wootecam.festivals.domain.ticket.service.TicketService;
import com.wootecam.festivals.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivals/{festivalId}/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<TicketIdResponse> createTicket(@PathVariable Long festivalId,
                                                      @Valid @RequestBody TicketCreateRequest request) {
        Long saveTicketId = ticketService.createTicket(festivalId, request);

        return ApiResponse.of(new TicketIdResponse(saveTicketId));
    }
}
