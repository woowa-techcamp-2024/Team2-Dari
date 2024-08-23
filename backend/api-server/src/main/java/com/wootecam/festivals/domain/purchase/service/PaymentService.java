package com.wootecam.festivals.domain.purchase.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {

    @Async
    public void pay(Long memberId, Long ticketId) {
        log.debug("결제 요청 - 회원 ID: {}, 티켓 ID: {}", memberId, ticketId);
        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).thenRun(() -> log.debug("결제 완료 - 회원 ID: {}, 티켓 ID: {}", memberId, ticketId));
    }
}
