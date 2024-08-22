package com.wootecam.festivals.domain.ticket.dto;

import java.time.LocalDateTime;

/**
 * DTO for {@link com.wootecam.festivals.domain.ticket.entity.Ticket}
 */
public record TicketResponse(Long id,
                             String name, String detail,
                             Long price, int quantity, int remainStock,
                             LocalDateTime startSaleTime, LocalDateTime endSaleTime,
                             LocalDateTime refundEndTime,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
}
