package com.wootecam.festivals.domain.ticket.entity;

import java.time.LocalDateTime;

public record TicketInfo (LocalDateTime startSaleTime, LocalDateTime endSaleTime) {
}
