package com.wootecam.festivals.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.wootecam.festivals.domain.payment.excpetion.PaymentErrorCode;
import com.wootecam.festivals.domain.payment.service.PaymentService.PaymentStatus;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

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
        @DisplayName("유효한 memberId와 ticketId가 주어졌을 때")
        class Context_with_valid_memberId_and_ticketId {

            @Test
            @DisplayName("결제 ID를 반환한다")
            void it_returns_payment_id() {
                // When
                String paymentId = paymentService.initiatePayment(1L, 1L);

                // Then
                assertThat(paymentId).isNotNull().isNotEmpty();
            }

            @Test
            @DisplayName("결제 상태를 PENDING으로 설정한다")
            void it_sets_payment_status_to_pending() {
                // When
                String paymentId = paymentService.initiatePayment(1L, 1L);

                // Then
                assertThat(paymentService.getPaymentStatus(paymentId)).isEqualTo(PaymentStatus.PENDING);
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
                paymentId = paymentService.initiatePayment(1L, 1L);
            }

            @Test
            @DisplayName("해당 결제의 상태를 반환한다")
            void it_returns_payment_status() {
                // When
                PaymentStatus status = paymentService.getPaymentStatus(paymentId);

                // Then
                assertThat(status).isNotNull();
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
    @DisplayName("processPayment 메소드는")
    class Describe_processPayment {

        @Nested
        @DisplayName("결제 처리가 완료되었을 때")
        class Context_when_payment_processing_is_completed {

            @Test
            @DisplayName("결제 상태를 SUCCESS, PENDING, 또는 FAILED로 업데이트한다")
            void it_updates_payment_status_to_success_pending_or_failed() {
                // Given
                String paymentId = paymentService.initiatePayment(1L, 1L);

                // When & Then
                await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                    PaymentStatus status = paymentService.getPaymentStatus(paymentId);
                    assertThat(status).isIn(PaymentStatus.SUCCESS, PaymentStatus.PENDING, PaymentStatus.FAILED);
                });
            }
        }
    }

    @Nested
    @DisplayName("simulateExternalPaymentApi 메소드는")
    class Describe_simulateExternalPaymentApi {

        @Test
        @DisplayName("SUCCESS, PENDING, 또는 FAILED 중 하나의 상태를 반환한다")
        void it_returns_one_of_success_pending_or_failed_status() {
            // Given
            PaymentService.PaymentStatus status = ReflectionTestUtils.invokeMethod(paymentService,
                    "simulateExternalPaymentApi");

            // Then
            assertThat(status).isIn(PaymentStatus.SUCCESS, PaymentStatus.PENDING, PaymentStatus.FAILED);
        }
    }
}