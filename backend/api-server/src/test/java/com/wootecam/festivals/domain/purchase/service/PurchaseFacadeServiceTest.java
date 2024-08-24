package com.wootecam.festivals.domain.purchase.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.payment.service.PaymentService;
import com.wootecam.festivals.domain.ticket.dto.CachedTicketInfo;
import com.wootecam.festivals.domain.ticket.service.TicketCacheService;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.service.QueueService;
import com.wootecam.festivals.global.utils.TimeProvider;
import java.time.LocalDateTime;
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

    @Mock
    private PaymentService paymentService;

    @Mock
    private TicketCacheService ticketCacheService;

    @Mock
    private QueueService queueService;
    private final Long ticketId = 1L;

    @Mock
    private TimeProvider timeProvider;
    private final Long festivalId = 1L;
    @InjectMocks
    private PurchaseFacadeService purchaseFacadeService;
    @Mock
    private TicketStockRollbacker ticketReserveCanceler;

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
                when(paymentService.initiatePayment(memberId, ticketId)).thenReturn("payment-123");
            }

            @Test
            @DisplayName("결제 ID를 반환한다")
            void it_returns_payment_id() {
                String result = purchaseFacadeService.processPurchase(new PurchaseData(memberId, ticketId));

                assertThat(result).isEqualTo("payment-123");
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
                        purchaseFacadeService.processPurchase(new PurchaseData(memberId, ticketId)));
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
                when(paymentService.getPaymentStatus("payment-123")).thenReturn(PaymentService.PaymentStatus.SUCCESS);
            }

            @Test
            @DisplayName("결제 상태를 반환한다")
            void it_returns_payment_status() {
                PaymentService.PaymentStatus result = purchaseFacadeService.getPaymentStatus("payment-123");

                assertThat(result).isEqualTo(PaymentService.PaymentStatus.SUCCESS);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 결제 ID가 주어졌을 때")
        class Context_with_invalid_payment_id {

            @BeforeEach
            void setUp() {
                when(paymentService.getPaymentStatus("invalid-id")).thenReturn(null);
            }

            @Test
            @DisplayName("예외를 던진다")
            void it_throws_exception() {
                assertThrows(ApiException.class, () ->
                        purchaseFacadeService.getPaymentStatus("invalid-id"));
            }
        }
    }

    @Nested
    @DisplayName("processPaymentResults 메소드는")
    class Describe_processPaymentResults {

        @BeforeEach
        void setUp() {
            CachedTicketInfo mockTicketInfo = mock(CachedTicketInfo.class);
            when(mockTicketInfo.startSaleTime()).thenReturn(LocalDateTime.now().minusDays(1));
            when(mockTicketInfo.endSaleTime()).thenReturn(LocalDateTime.now().plusDays(1));
            when(ticketCacheService.getTicketInfo(anyLong())).thenReturn(mockTicketInfo);

            when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
            when(paymentService.initiatePayment(anyLong(), anyLong())).thenReturn("payment-123");

            PurchaseData purchaseData = new PurchaseData(memberId, ticketId);
            purchaseFacadeService.processPurchase(purchaseData);
        }

        @Nested
        @DisplayName("결제가 성공했을 때")
        class Context_when_payment_succeeds {

            @BeforeEach
            void setUp() {
                when(paymentService.getPaymentStatus(any())).thenReturn(PaymentService.PaymentStatus.SUCCESS);
            }

            @Test
            @DisplayName("구매 데이터를 큐에 추가한다")
            void it_adds_purchase_data_to_queue() {
                purchaseFacadeService.processPaymentResults();

                verify(queueService, times(1)).addPurchase(any());
            }
        }

        @Nested
        @DisplayName("결제가 실패했을 때")
        class Context_when_payment_fails {

            @BeforeEach
            void setUp() {
                when(paymentService.getPaymentStatus(any())).thenReturn(PaymentService.PaymentStatus.FAILED);
            }

            @Test
            @DisplayName("티켓 재고를 롤백한다")
            void it_rollbacks_ticket_stock() {
                purchaseFacadeService.processPaymentResults();

                verify(ticketReserveCanceler, times(1)).rollbackTicketStock(ticketId);
            }
        }
    }
}