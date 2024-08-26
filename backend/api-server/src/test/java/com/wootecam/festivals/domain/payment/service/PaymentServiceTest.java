package com.wootecam.festivals.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.wootecam.festivals.domain.payment.excpetion.PaymentErrorCode;
import com.wootecam.festivals.domain.payment.service.PaymentService.PaymentStatus;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

@DisplayName("PaymentService 클래스")
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("initiatePayment 메소드는")
    class Describe_initiatePayment {

        @Nested
        @DisplayName("유효한 paymentId, memberId와 ticketId가 주어졌을 때")
        class Context_with_valid_paymentId_memberId_and_ticketId {

            @Test
            @DisplayName("CompletableFuture<PaymentStatus>를 반환한다")
            void it_returns_completable_future_of_payment_status() {
                // Given
                String paymentId = UUID.randomUUID().toString();

                // When
                CompletableFuture<PaymentStatus> future = paymentService.initiatePayment(paymentId, 1L, 1L);

                // Then
                assertThat(future).isNotNull();

                await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
                    assertThat(future.isDone()).isTrue();
                    assertThat(future.get()).isIn(PaymentStatus.SUCCESS, PaymentStatus.FAILED);
                });
            }
        }
    }

    @Nested
    @DisplayName("getPaymentStatus 메소드는")
    class Describe_getPaymentStatus {


        @Nested
        @DisplayName("존재하는 결제 ID가 주어졌을 때")
        class Context_with_existing_payment_id {

            private String paymentId;

            @BeforeEach
            void setUp() {
                paymentId = UUID.randomUUID().toString();
                // PENDING 상태로 초기화
                paymentService.updatePaymentStatus(paymentId, PaymentStatus.PENDING);
                // CompletableFuture 완료 대기
                paymentService.initiatePayment(paymentId, 1L, 1L).join();
            }

            @Test
            @DisplayName("해당 결제의 상태를 반환한다")
            void it_returns_payment_status() {
                // When & Then
                await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
                    PaymentStatus status = paymentService.getPaymentStatus(paymentId);
                    assertThat(status).isIn(PaymentStatus.SUCCESS, PaymentStatus.FAILED);
                });
            }
        }

        @Nested
        @DisplayName("존재하지 않는 결제 ID가 주어졌을 때")
        class Context_with_non_existing_payment_id {

            @Test
            @DisplayName("ApiException을 던진다")
            void it_throws_ApiException() {
                // When & Then
                assertThatThrownBy(() -> paymentService.getPaymentStatus("non-existing-id"))
                        .isInstanceOf(ApiException.class)
                        .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.PAYMENT_NOT_EXIST);
            }
        }
    }

    @Nested
    @DisplayName("updatePaymentStatus 메소드는")
    class Describe_updatePaymentStatus {

        @Test
        @DisplayName("결제 상태를 업데이트한다")
        void it_updates_payment_status() {
            // Given
            String paymentId = UUID.randomUUID().toString();
            PaymentStatus newStatus = PaymentStatus.SUCCESS;

            // When
            paymentService.updatePaymentStatus(paymentId, newStatus);

            // Then
            assertThat(paymentService.getPaymentStatus(paymentId)).isEqualTo(newStatus);
        }
    }
}