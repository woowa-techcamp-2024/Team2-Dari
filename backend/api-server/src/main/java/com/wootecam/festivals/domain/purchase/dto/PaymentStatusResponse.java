package com.wootecam.festivals.domain.purchase.dto;

import com.wootecam.festivals.domain.payment.service.PaymentService;

public record PaymentStatusResponse(PaymentService.PaymentStatus paymentStatus) {
}
