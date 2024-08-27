package com.wootecam.festivals.domain.purchase.entity;

import java.time.LocalDateTime;

public record TicketInfo (LocalDateTime startSaleTime, LocalDateTime endSaleTime) {
}
