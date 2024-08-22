package com.wootecam.festivals.domain.ticket.dto;

import java.time.LocalDateTime;

public record TicketWithoutStockResponse(Long id,
                             String name, String detail,
                             Long price, int quantity,
                             LocalDateTime startSaleTime, LocalDateTime endSaleTime,
                             LocalDateTime refundEndTime,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
}
