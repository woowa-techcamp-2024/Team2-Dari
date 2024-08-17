package com.wootecam.festivals.domain.ticket.service;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.dto.TicketIdResponse;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 티켓 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;
    private final FestivalRepository festivalRepository;

    /**
     * 티켓 생성
     *
     * @param festivalId 축제 ID
     * @param request    티켓 생성 요청 DTO
     * @return 생성된 티켓의 ID
     */
    @Transactional
    public TicketIdResponse createTicket(Long festivalId, TicketCreateRequest request) {
        log.debug("티켓 생성 요청 - 축제 ID: {}", festivalId);

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> {
                    log.warn("축제를 찾을 수 없음 - 축제 ID: {}", festivalId);
                    return new ApiException(FestivalErrorCode.FESTIVAL_NOT_FOUND);
                });

        Ticket newTicket = ticketRepository.save(request.toEntity(festival));
        log.debug("티켓 엔티티 생성 - 티켓 ID: {}", newTicket.getId());

        ticketStockRepository.save(newTicket.createTicketStock());
        log.debug("티켓 재고 생성 완료 - 티켓 ID: {}", newTicket.getId());

        TicketIdResponse response = new TicketIdResponse(newTicket.getId());
        log.debug("티켓 생성 완료 - 티켓 ID: {}", response.ticketId());

        return response;
    }
}