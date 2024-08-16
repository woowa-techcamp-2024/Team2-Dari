package com.wootecam.festivals.domain.purchase.service;

import static com.wootecam.festivals.utils.Fixture.createFestival;
import static com.wootecam.festivals.utils.Fixture.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.dto.PurchaseIdResponse;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PurchaseServiceTest extends SpringBootTestConfig {

    private final PurchaseService purchaseService;
    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;
    private final PurchaseRepository purchaseRepository;

    private LocalDateTime ticketSaleStartTime = LocalDateTime.now();
    private Festival festival;
    private Member member;

    @Autowired
    public PurchaseServiceTest(PurchaseService purchaseService, TicketRepository ticketRepository,
                               FestivalRepository festivalRepository,
                               TicketStockRepository ticketStockRepository, MemberRepository memberRepository,
                               PurchaseRepository purchaseRepository) {
        this.purchaseService = purchaseService;
        this.ticketRepository = ticketRepository;
        this.ticketStockRepository = ticketStockRepository;
        this.purchaseRepository = purchaseRepository;

        festival = festivalRepository.save(createFestival("Test Festival", "Test Festival Detail",
                ticketSaleStartTime, ticketSaleStartTime.plusDays(4)));
        member = memberRepository.save(createMember("purchaser", "purchaser@example.com"));
    }

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(purchaseRepository);
        TestDBCleaner.clear(ticketStockRepository);
        TestDBCleaner.clear(ticketRepository);
    }

    @Nested
    @DisplayName("티켓 구매 시")
    class Describe_createPurchase {

        Ticket ticket;

        @BeforeEach
        void setUp() {
            ticket = ticketRepository.save(Ticket.builder()
                    .name("Test Ticket")
                    .detail("Test Ticket Detail")
                    .price(10000L)
                    .quantity(100)
                    .startSaleTime(ticketSaleStartTime)
                    .endSaleTime(ticketSaleStartTime.plusDays(2))
                    .refundEndTime(ticketSaleStartTime.plusDays(2))
                    .festival(festival)
                    .build());
            ticketStockRepository.save(ticket.createTicketStock());
        }

        @Test
        @DisplayName("티켓을 구매할 수 있다.")
        void It_return_new_purchase() {
            PurchaseIdResponse response = purchaseService.createPurchase(ticket.getId(), member.getId(),
                    LocalDateTime.now());

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertThat(ticketStockRepository.findByTicket(ticket)).isPresent()
                            .get()
                            .extracting(TicketStock::getRemainStock)
                            .isEqualTo(ticket.getQuantity() - 1)
            );
        }

        @Nested
        @DisplayName("티켓 재고가 없으면")
        class Context_with_no_stock {

            Ticket ticket;

            @BeforeEach
            void setUp() {
                ticket = ticketRepository.save(Ticket.builder()
                        .name("Test Ticket")
                        .detail("Test Ticket Detail")
                        .price(10000L)
                        .quantity(100)
                        .startSaleTime(ticketSaleStartTime)
                        .endSaleTime(ticketSaleStartTime.plusDays(2))
                        .refundEndTime(ticketSaleStartTime.plusDays(2))
                        .festival(festival)
                        .build());
                ticketStockRepository.save(TicketStock.builder()
                        .ticket(ticket)
                        .remainStock(0)
                        .build());
            }

            @Test
            @DisplayName("예외가 발생한다")
            void It_throws_exception() {
                assertThatThrownBy(
                        () -> purchaseService.createPurchase(ticket.getId(), member.getId(), LocalDateTime.now()))
                        .isInstanceOf(ApiException.class)
                        .hasMessage(TicketErrorCode.TICKET_STOCK_EMPTY.getMessage());
            }
        }

        @Nested
        @DisplayName("티켓 구매 시각 이전이라면")
        class Context_with_before_purchase_time {

            Ticket ticket;

            @BeforeEach
            void setUp() {
                ticket = ticketRepository.save(Ticket.builder()
                        .name("Test Ticket")
                        .detail("Test Ticket Detail")
                        .price(10000L)
                        .quantity(100)
                        .startSaleTime(ticketSaleStartTime)
                        .endSaleTime(ticketSaleStartTime.plusDays(2))
                        .refundEndTime(ticketSaleStartTime.plusDays(2))
                        .festival(festival)
                        .build());
                ticketStockRepository.save(ticket.createTicketStock());
            }

            @Test
            @DisplayName("예외가 발생한다")
            void It_throws_exception() {
                LocalDateTime now = ticketSaleStartTime.minusMinutes(1);

                assertThatThrownBy(() -> purchaseService.createPurchase(ticket.getId(), member.getId(), now))
                        .isInstanceOf(ApiException.class)
                        .hasMessage(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME.getMessage());
            }
        }

        @Nested
        @DisplayName("티켓 구매 시각 이후라면")
        class Context_with_after_purchase_time {

            Ticket ticket;

            @BeforeEach
            void setUp() {
                ticket = ticketRepository.save(Ticket.builder()
                        .name("Test Ticket")
                        .detail("Test Ticket Detail")
                        .price(10000L)
                        .quantity(100)
                        .startSaleTime(ticketSaleStartTime)
                        .endSaleTime(ticketSaleStartTime.plusDays(2))
                        .refundEndTime(ticketSaleStartTime.plusDays(2))
                        .festival(festival)
                        .build());
                ticketStockRepository.save(ticket.createTicketStock());
            }

            @Test
            @DisplayName("예외가 발생한다")
            void It_throws_exception() {
                LocalDateTime now = ticket.getEndSaleTime().plusMinutes(1);

                assertThatThrownBy(() -> purchaseService.createPurchase(ticket.getId(), member.getId(), now))
                        .isInstanceOf(ApiException.class)
                        .hasMessage(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME.getMessage());
            }
        }

        @Nested
        @DisplayName("티켓을 이미 구매했다면")
        class Context_with_already_purchase_ticket {

            Ticket ticket;

            @BeforeEach
            void setUp() {
                ticket = ticketRepository.save(Ticket.builder()
                        .name("Test Ticket")
                        .detail("Test Ticket Detail")
                        .price(10000L)
                        .quantity(100)
                        .startSaleTime(ticketSaleStartTime)
                        .endSaleTime(ticketSaleStartTime.plusDays(2))
                        .refundEndTime(ticketSaleStartTime.plusDays(2))
                        .festival(festival)
                        .build());
                ticketStockRepository.save(ticket.createTicketStock());
                purchaseRepository.save(Purchase.builder()
                        .ticket(ticket)
                        .purchaseStatus(PurchaseStatus.PURCHASED)
                        .purchaseTime(LocalDateTime.now())
                        .member(member)
                        .build());
            }

            @Test
            @DisplayName("예외가 발생한다")
            void It_throws_exception() {
                assertThatThrownBy(
                        () -> purchaseService.createPurchase(ticket.getId(), member.getId(), LocalDateTime.now()))
                        .isInstanceOf(ApiException.class)
                        .hasMessage(PurchaseErrorCode.ALREADY_PURCHASED_TICKET.getMessage());
            }
        }
    }
}