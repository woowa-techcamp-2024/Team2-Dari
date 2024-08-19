package com.wootecam.festivals.domain.purchase.dto;

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
                                          LocalDateTime endSaleTime) {
}
