package com.wootecam.festivals.domain.payment.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wootecam.festivals.domain.payment.excpetion.PaymentErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {

    // 결제 ID와 상태를 저장하는 인메모리 캐시
    private final Cache<String, PaymentStatus> paymentStatusCache;

    public PaymentService() {
        // Caffeine 캐시 설정: 1시간 후 만료되는 캐시 생성
        this.paymentStatusCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    // 결제 프로세스를 시작하는 메서드
    public CompletableFuture<PaymentStatus> initiatePayment(String paymentId, Long memberId, Long ticketId) {
        paymentStatusCache.put(paymentId, PaymentStatus.PENDING);
        return CompletableFuture.supplyAsync(() -> processPayment(paymentId, memberId, ticketId));
    }

    // 실제 결제 처리를 수행하는 private 메서드
    private PaymentStatus processPayment(String paymentId, Long memberId, Long ticketId) {
        log.debug("결제 처리 중 - 결제 ID: {}, 회원 ID: {}, 티켓 ID: {}", paymentId, memberId, ticketId);
        try {
            // 결제 처리 시뮬레이션
            Thread.sleep(5000); // 5초 대기
            // 결제 결과 시뮬레이션
            PaymentStatus result = simulateExternalPaymentApi();
            // 결제 결과를 캐시에 저장
            paymentStatusCache.put(paymentId, result);
            log.debug("결제 완료 - 결제 ID: {}, 회원 ID: {}, 티켓 ID: {}, 상태: {}", paymentId, memberId, ticketId, result);
            return result;
        } catch (InterruptedException e) {
            log.error("결제 처리 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            paymentStatusCache.put(paymentId, PaymentStatus.FAILED);
            return PaymentStatus.FAILED;
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            paymentStatusCache.put(paymentId, PaymentStatus.FAILED);
            return PaymentStatus.FAILED;
        }
    }

    public void updatePaymentStatus(String paymentId, PaymentStatus status) {
        paymentStatusCache.put(paymentId, status);
    }

    // 결제 상태를 조회하는 메서드
    public PaymentStatus getPaymentStatus(String paymentId) {
        // 캐시에서 결제 상태를 조회하여 반환
        PaymentStatus result = paymentStatusCache.getIfPresent(paymentId);
        if (result == null) {
            throw new ApiException(PaymentErrorCode.PAYMENT_NOT_EXIST);
        }
        return result;
    }

    // 외부 결제 API 호출을 시뮬레이션하는 메서드
    private PaymentStatus simulateExternalPaymentApi() {
        // 랜덤으로 결제 결과 생성 (실제 구현에서는 제거됨)
//        double random = Math.random();
//        if (random < 0.9) {
//            return PaymentStatus.SUCCESS;
//        } else {
//            return PaymentStatus.FAILED;
//        }
        return PaymentStatus.SUCCESS;

    }

    // 결제 상태를 나타내는 열거형
    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED
    }
}