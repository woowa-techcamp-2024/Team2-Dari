package com.wootecam.festivals.domain.my.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.domain.festival.dto.FestivalResponse;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.ticket.dto.TicketWithoutStockResponse;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record MyPurchasedTicketResponse(
        Long purchaseId,
        @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
        LocalDateTime purchaseTime,
        PurchaseStatus purchaseStatus,
        TicketWithoutStockResponse ticket,
        FestivalResponse festival,
        Long checkinId,
        boolean isCheckedIn,
        @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
        LocalDateTime checkinTime
) {
}
