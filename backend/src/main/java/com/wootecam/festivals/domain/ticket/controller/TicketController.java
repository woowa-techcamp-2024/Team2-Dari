package com.wootecam.festivals.domain.ticket.controller;

import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.dto.TicketIdResponse;
import com.wootecam.festivals.domain.ticket.dto.TicketListResponse;
import com.wootecam.festivals.domain.ticket.service.TicketService;
import com.wootecam.festivals.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 티켓 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/festivals/{festivalId}/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * 티켓 생성 API
     *
     * @param festivalId 축제 ID
     * @param request    티켓 생성 요청 데이터
     * @return 생성된 티켓 ID 응답
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<TicketIdResponse> createTicket(@PathVariable Long festivalId,
                                                      @Valid @RequestBody TicketCreateRequest request) {
        log.debug("티켓 생성 요청 - 축제 ID: {}", festivalId);
        TicketIdResponse response = ticketService.createTicket(festivalId, request);
        log.debug("티켓 생성 완료 - 축제 ID: {}, 티켓 ID: {}", festivalId, response.ticketId());
        return ApiResponse.of(response);
    }

    /**
     * 티켓 목록 조회 API
     *
     * @param festivalId
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ApiResponse<TicketListResponse> getTickets(@PathVariable Long festivalId) {
        log.debug("티켓 목록 조회 요청 - 축제 ID: {}", festivalId);
        TicketListResponse tickets = ticketService.getTickets(festivalId);
        log.debug("조회된 티켓 목록: {}", tickets);
        return ApiResponse.of(tickets);
    }
}
