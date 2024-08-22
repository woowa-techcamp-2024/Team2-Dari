package com.wootecam.festivals.domain.purchase.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record PurchasePreviewInfoResponse(Long festivalId,
                                          String festivalTitle,
                                          String festivalImg,
                                          Long ticketId,
                                          String ticketName,
                                          String ticketDetail,
                                          Long ticketPrice,
                                          int ticketQuantity,
                                          int remainTicketQuantity,
                                          @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
                                          LocalDateTime endSaleTime) {
}
