package com.wootecam.festivals.domain.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.payment.excpetion.PaymentErrorCode;
import com.wootecam.festivals.domain.payment.service.PaymentService;
import com.wootecam.festivals.domain.payment.service.PaymentService.PaymentStatus;
import com.wootecam.festivals.domain.ticket.dto.CachedTicketInfo;
import com.wootecam.festivals.domain.ticket.service.TicketCacheService;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.service.QueueService;
import com.wootecam.festivals.global.utils.TimeProvider;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseFacadeServiceTest {

    private final Long memberId = 1L;
    private final Long ticketId = 1L;
    private final Long festivalId = 1L;
    private final Long ticketStockId = 1L;

    @Mock
    private PaymentService paymentService;
    @Mock
    private TicketCacheService ticketCacheService;
    @Mock
    private QueueService queueService;
    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private PurchaseFacadeService purchaseFacadeService;

    @Nested
    @DisplayName("processPurchase 메소드는")
    class Describe_processPurchase {

        @Nested
        @DisplayName("유효한 구매 요청이 주어졌을 때")
        class Context_with_valid_purchase_request {

            @BeforeEach
            void setUp() {
                CachedTicketInfo ticketInfo = new CachedTicketInfo(ticketId, "Test Ticket", festivalId,
                        LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 10000L, 100);
                when(ticketCacheService.getTicketInfo(ticketId)).thenReturn(ticketInfo);
                when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
                when(paymentService.initiatePayment(anyString(), anyLong(), anyLong())).thenReturn(
                        CompletableFuture.completedFuture(PaymentStatus.SUCCESS));
            }

            @Test
            @DisplayName("결제 ID를 반환한다")
            void it_returns_payment_id() {
                String result = purchaseFacadeService.processPurchase(
                        new PurchaseData(memberId, ticketId, ticketStockId));

                assertThat(result).isNotNull().isNotEmpty();
            }
        }

        @Nested
        @DisplayName("유효하지 않은 구매 시간에")
        class Context_with_invalid_purchase_time {

            @BeforeEach
            void setUp() {
                CachedTicketInfo ticketInfo = new CachedTicketInfo(ticketId, "Test Ticket", festivalId,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), 10000L, 100);
                when(ticketCacheService.getTicketInfo(ticketId)).thenReturn(ticketInfo);
                when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
            }

            @Test
            @DisplayName("예외를 던진다")
            void it_throws_exception() {
                assertThrows(ApiException.class, () ->
                        purchaseFacadeService.processPurchase(new PurchaseData(memberId, ticketId, ticketStockId)));
            }
        }
    }

    @Nested
    @DisplayName("getPaymentStatus 메소드는")
    class Describe_getPaymentStatus {

        @Nested
        @DisplayName("유효한 결제 ID가 주어졌을 때")
        class Context_with_valid_payment_id {

            @BeforeEach
            void setUp() {
                when(paymentService.getPaymentStatus("payment-123")).thenReturn(PaymentStatus.SUCCESS);
            }

            @Test
            @DisplayName("결제 상태를 반환한다")
            void it_returns_payment_status() {
                PaymentStatus result = purchaseFacadeService.getPaymentStatus("payment-123");

                assertThat(result).isEqualTo(PaymentStatus.SUCCESS);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 결제 ID가 주어졌을 때")
        class Context_with_invalid_payment_id {

            @BeforeEach
            void setUp() {
                when(paymentService.getPaymentStatus("invalid-id")).thenThrow(
                        new ApiException(PaymentErrorCode.PAYMENT_NOT_EXIST));
            }

            @Test
            @DisplayName("예외를 던진다")
            void it_throws_exception() {
                assertThrows(ApiException.class, () ->
                        purchaseFacadeService.getPaymentStatus("invalid-id"));
            }
        }
    }

    // handlePaymentResult 메서드가 private이므로 직접 테스트할 수 없습니다.
    // 대신 processPurchase 메서드를 통해 간접적으로 테스트할 수 있습니다.
    @Nested
    @DisplayName("processPurchase 메소드의 비동기 처리는")
    class Describe_processPurchase_async {

        @BeforeEach
        void setUp() {
            CachedTicketInfo mockTicketInfo = mock(CachedTicketInfo.class);
            when(mockTicketInfo.startSaleTime()).thenReturn(LocalDateTime.now().minusDays(1));
            when(mockTicketInfo.endSaleTime()).thenReturn(LocalDateTime.now().plusDays(1));
            when(ticketCacheService.getTicketInfo(anyLong())).thenReturn(mockTicketInfo);
            when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
        }

        @Test
        @DisplayName("결제가 성공하면 구매 데이터를 큐에 추가한다")
        void it_adds_purchase_data_to_queue_when_payment_succeeds() throws Exception {
            when(paymentService.initiatePayment(anyString(), anyLong(), anyLong())).thenReturn(
                    CompletableFuture.completedFuture(PaymentStatus.SUCCESS));

            purchaseFacadeService.processPurchase(new PurchaseData(memberId, ticketId, ticketStockId));

            // 비동기 처리가 완료될 때까지 잠시 대기
            Thread.sleep(100);

            verify(queueService).addPurchase(any(PurchaseData.class));
        }

        @Test
        @DisplayName("결제가 실패하면 구매 데이터를 큐에 추가하지 않는다")
        void it_does_not_add_purchase_data_to_queue_when_payment_fails() throws Exception {
            when(paymentService.initiatePayment(anyString(), anyLong(), anyLong())).thenReturn(
                    CompletableFuture.completedFuture(PaymentStatus.FAILED));

            purchaseFacadeService.processPurchase(new PurchaseData(memberId, ticketId, ticketStockId));

            // 비동기 처리가 완료될 때까지 잠시 대기
            Thread.sleep(100);

            verify(queueService, never()).addPurchase(any(PurchaseData.class));
        }
    }
}