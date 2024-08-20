package com.wootecam.festivals.domain.purchase.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {

    @Async
    public void pay(Long memberId, Long ticketId) {
        log.debug("결제 요청 - 회원 ID: {}, 티켓 ID: {}", memberId, ticketId);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return;
        }
        log.debug("결제 완료 - 회원 ID: {}, 티켓 ID: {}", memberId, ticketId);
    }
}
