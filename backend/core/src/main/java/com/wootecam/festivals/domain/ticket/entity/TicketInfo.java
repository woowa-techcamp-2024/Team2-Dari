package com.wootecam.festivals.domain.ticket.entity;

import java.time.LocalDateTime;

public record TicketInfo (LocalDateTime startSaleTime, LocalDateTime endSaleTime) {

    public boolean isNotOnSale(LocalDateTime now) {
        return now.isBefore(startSaleTime) || now.isAfter(endSaleTime);
    }
}
