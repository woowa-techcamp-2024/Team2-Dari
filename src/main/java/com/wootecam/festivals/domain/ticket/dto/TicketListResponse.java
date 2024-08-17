package com.wootecam.festivals.domain.ticket.dto;

import java.util.List;

public record TicketListResponse(Long festivalId, List<TicketResponse> tickets) {
}
