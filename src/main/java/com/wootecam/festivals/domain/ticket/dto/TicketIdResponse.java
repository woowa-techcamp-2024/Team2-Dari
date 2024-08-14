package com.wootecam.festivals.domain.ticket.dto;

import java.io.Serializable;

/**
 * DTO for {@link com.wootecam.festivals.domain.ticket.entity.Ticket}
 */
public record TicketIdResponse(Long ticketId) implements Serializable {
}
