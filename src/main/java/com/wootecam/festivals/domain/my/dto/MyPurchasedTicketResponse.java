package com.wootecam.festivals.domain.my.dto;

import com.wootecam.festivals.domain.festival.dto.FestivalResponse;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.ticket.dto.TicketWithoutStockResponse;
import java.time.LocalDateTime;

public record MyPurchasedTicketResponse(
        Long purchaseId,
        LocalDateTime purchaseTime,
        PurchaseStatus purchaseStatus,
        TicketWithoutStockResponse ticket,
        FestivalResponse festival
) {
}
