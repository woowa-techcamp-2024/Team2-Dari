package com.wootecam.festivals.domain.payment.dto;

import com.wootecam.festivals.domain.payment.service.PaymentService.PaymentStatus;

public record PaymentInfo(String paymentId,
                          Long memberId,
                          Long ticketId,
                          Long ticketStockId,
                          PaymentStatus status) {

}
