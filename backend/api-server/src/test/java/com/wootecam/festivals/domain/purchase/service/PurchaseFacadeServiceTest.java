package com.wootecam.festivals.domain.purchase.service;

import static com.wootecam.festivals.utils.Fixture.createFestival;
import static com.wootecam.festivals.utils.Fixture.createMember;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.payment.service.PaymentService;
import com.wootecam.festivals.domain.payment.service.PaymentService.PaymentStatus;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.ticket.dto.CachedTicketInfo;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.domain.ticket.service.TicketCacheService;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.service.QueueService;
import com.wootecam.festivals.global.utils.TimeProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PurchaseFacadeServiceTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private TicketCacheService ticketCacheService;

    @Mock
    private QueueService queueService;

    @Mock
    private TicketStockRepository ticketStockRepository;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private PurchaseFacadeService purchaseFacadeService;

    private LocalDateTime ticketSaleStartTime;
    private Festival festival;
    private Member member;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ticketSaleStartTime = LocalDateTime.now().minusHours(1);

        member = createMember("admin", "test@test.com");
        festival = createFestival(member, "Test Festival", "Test Festival Detail",
                ticketSaleStartTime.plusDays(1), ticketSaleStartTime.plusDays(4));
        ticket = Ticket.builder()
                .name("Test Ticket")
                .detail("Test Ticket Detail")
                .price(10000L)
                .quantity(100)
                .startSaleTime(ticketSaleStartTime)
                .endSaleTime(ticketSaleStartTime.plusDays(2))
                .refundEndTime(ticketSaleStartTime.plusDays(2))
                .festival(festival)
                .build();
    }

    @Test
    @DisplayName("결제 실패 시 재고를 원복한다")
    void it_compensates_stock_on_payment_failure() {
        // Given
        when(ticketStockRepository.findByTicket(ticket)).thenReturn(Optional.of(ticket.createTicketStock()));
        when(ticketCacheService.getTicketInfo(ticket.getId()))
                .thenReturn(new CachedTicketInfo(ticket.getId(), ticket.getName(), ticket.getFestival().getId(),
                        ticket.getStartSaleTime(), ticket.getEndSaleTime(), ticket.getPrice(), ticket.getQuantity()));
        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
        when(ticketStockRepository.decreaseStockAtomically(ticket.getId())).thenReturn(1);

        PurchaseData purchaseData = new PurchaseData(member.getId(), ticket.getId());
        String paymentId = "mockPaymentId";

        when(paymentService.initiatePayment(member.getId(), ticket.getId())).thenReturn(paymentId);
        when(paymentService.getPaymentStatus(paymentId)).thenReturn(PaymentStatus.FAILED);

        purchaseFacadeService.processPurchase(purchaseData);

        // When
        purchaseFacadeService.processPaymentResults();

        // Then
        verify(ticketStockRepository, times(1)).increaseStockAtomically(ticket.getId());
        verify(queueService, never()).addPurchase(any(PurchaseData.class));
    }

    @Test
    @DisplayName("결제 성공 시 구매 정보를 큐에 추가한다")
    void it_adds_purchase_to_queue_on_payment_success() {
        // Given
        when(ticketStockRepository.findByTicket(ticket)).thenReturn(Optional.of(ticket.createTicketStock()));
        when(ticketCacheService.getTicketInfo(ticket.getId()))
                .thenReturn(new CachedTicketInfo(ticket.getId(), ticket.getName(), ticket.getFestival().getId(),
                        ticket.getStartSaleTime(), ticket.getEndSaleTime(), ticket.getPrice(), ticket.getQuantity()));
        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
        when(ticketStockRepository.decreaseStockAtomically(ticket.getId())).thenReturn(1);

        PurchaseData purchaseData = new PurchaseData(member.getId(), ticket.getId());
        String paymentId = "mockPaymentId";

        when(paymentService.initiatePayment(member.getId(), ticket.getId())).thenReturn(paymentId);
        when(paymentService.getPaymentStatus(paymentId)).thenReturn(PaymentStatus.SUCCESS);

        purchaseFacadeService.processPurchase(purchaseData);

        // When
        purchaseFacadeService.processPaymentResults();

        // Then
        verify(queueService, times(1)).addPurchase(purchaseData);
        verify(ticketStockRepository, never()).increaseStockAtomically(ticket.getId());
    }

    @Test
    @DisplayName("구매 가능 시간이 아닐 경우 예외를 던진다")
    void it_throws_exception_when_ticket_is_not_available() {
        // Given
        when(ticketCacheService.getTicketInfo(ticket.getId()))
                .thenReturn(new CachedTicketInfo(ticket.getId(), ticket.getName(), ticket.getFestival().getId(),
                        ticket.getStartSaleTime().plusHours(2), ticket.getEndSaleTime(), ticket.getPrice(),
                        ticket.getQuantity()));

        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());

        PurchaseData purchaseData = new PurchaseData(member.getId(), ticket.getId());

        // When & Then
        assertThatThrownBy(() -> purchaseFacadeService.processPurchase(purchaseData))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME);
    }
}
