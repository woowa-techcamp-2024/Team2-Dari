package com.wootecam.festivals.domain.purchase.service;

import static com.wootecam.festivals.utils.Fixture.createFestival;
import static com.wootecam.festivals.utils.Fixture.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.wootecam.festivals.domain.checkin.repository.CheckinRepository;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.exception.MemberErrorCode;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.dto.PurchaseIdResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchasePreviewInfoResponse;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PurchaseServiceTest extends SpringBootTestConfig {

    private final PurchaseService purchaseService;
    private final MemberRepository memberRepository;
    private final FestivalRepository festivalRepository;
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
                               PurchaseRepository purchaseRepository, CheckinRepository checkinRepository) {
        this.purchaseService = purchaseService;
        this.memberRepository = memberRepository;
        this.festivalRepository = festivalRepository;
        this.ticketRepository = ticketRepository;
        this.ticketStockRepository = ticketStockRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @BeforeEach
    void setUp() {
        clear();

        Member admin = memberRepository.save(createMember("admin", "admin@test.com"));
        festival = festivalRepository.save(createFestival(admin, "Test Festival", "Test Festival Detail",
                ticketSaleStartTime.plusDays(1), ticketSaleStartTime.plusDays(4)));
    }

    @Nested
    @DisplayName("티켓 구매 시")
    class Describe_createPurchase {

        Ticket ticket;

        @BeforeEach
        void setUp() {
            member = memberRepository.save(createMember("purchaser", "purchaser@example.com"));
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

    @Nested
    @DisplayName("티켓 구매 미리보기 정보 조회 시")
    class Get_purchase_preview_info {

        Member purchaser;

        @BeforeEach
        void setUp() {
            purchaser = memberRepository.save(createMember("purchaser", "purchaser@email.com"));
        }

        @Test
        @DisplayName("티켓 구매 미리보기 정보를 조회할 수 있다.")
        void It_return_purchase_preview_info() {
            Ticket ticket = ticketRepository.save(Ticket.builder()
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

            PurchasePreviewInfoResponse response = purchaseService.getPurchasePreviewInfo(purchaser.getId(),
                    festival.getId(), ticket.getId());

            assertAll(() -> assertThat(response.festivalId()).isEqualTo(festival.getId()),
                    () -> assertThat(response.ticketId()).isEqualTo(ticket.getId()),
                    () -> assertThat(response.ticketName()).isEqualTo(ticket.getName()),
                    () -> assertThat(response.ticketDetail()).isEqualTo(ticket.getDetail()),
                    () -> assertThat(response.ticketPrice()).isEqualTo(ticket.getPrice()),
                    () -> assertThat(response.ticketQuantity()).isEqualTo(ticket.getQuantity()),
                    () -> assertThat(response.festivalTitle()).isEqualTo(festival.getTitle()),
                    () -> assertThat(response.festivalImg()).isEqualTo(festival.getFestivalImg()),
                    () -> assertThat(response.remainTicketQuantity()).isEqualTo(ticket.getQuantity()),
                    () -> assertThat(response.endSaleTime()).isCloseTo(ticket.getEndSaleTime(), within(59, ChronoUnit.SECONDS))
            );
        }

        @Test
        @DisplayName("페스티벌의 티켓을 찾을 수 없으면 예외를 던진다.")
        void It_throws_exception_when_ticket_not_found() {
            assertThatThrownBy(() -> purchaseService.getPurchasePreviewInfo(purchaser.getId(), festival.getId(), 1L))
                    .isInstanceOf(ApiException.class)
                    .hasMessage(TicketErrorCode.TICKET_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("회원을 찾을 수 없으면 예외를 던진다.")
        void It_throws_exception_when_member_not_found() {
            Ticket ticket = ticketRepository.save(Ticket.builder()
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

            assertThatThrownBy(() -> purchaseService.getPurchasePreviewInfo(0L, festival.getId(), ticket.getId()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage(MemberErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("티켓 재고를 찾을 수 없으면 예외를 던진다.")
        void It_throws_exception_when_ticket_stock_not_found() {
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .name("Test Ticket")
                    .detail("Test Ticket Detail")
                    .price(10000L)
                    .quantity(100)
                    .startSaleTime(ticketSaleStartTime)
                    .endSaleTime(ticketSaleStartTime.plusDays(2))
                    .refundEndTime(ticketSaleStartTime.plusDays(2))
                    .festival(festival)
                    .build());

            assertThatThrownBy(
                    () -> purchaseService.getPurchasePreviewInfo(purchaser.getId(), festival.getId(), ticket.getId()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage(TicketErrorCode.TICKET_STOCK_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("티켓 구매 시각 이전이라면 예외를 던진다.")
        void It_throws_exception_when_before_purchase_time() {
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .name("Test Ticket")
                    .detail("Test Ticket Detail")
                    .price(10000L)
                    .quantity(100)
                    .startSaleTime(ticketSaleStartTime.plusMinutes(1))
                    .endSaleTime(ticketSaleStartTime.plusDays(2))
                    .refundEndTime(ticketSaleStartTime.plusDays(2))
                    .festival(festival)
                    .build());
            ticketStockRepository.save(ticket.createTicketStock());

            assertThatThrownBy(
                    () -> purchaseService.getPurchasePreviewInfo(purchaser.getId(), festival.getId(), ticket.getId()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME.getMessage());
        }

        @Test
        @DisplayName("티켓을 이미 구매했다면 예외를 던진다.")
        void It_throws_exception_when_already_purchased_ticket() {
            Ticket ticket = ticketRepository.save(Ticket.builder()
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
                    .member(purchaser)
                    .build());

            assertThatThrownBy(
                    () -> purchaseService.getPurchasePreviewInfo(purchaser.getId(), festival.getId(), ticket.getId()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage(PurchaseErrorCode.ALREADY_PURCHASED_TICKET.getMessage());
        }

        @Test
        @DisplayName("티켓 재고가 없다면 예외를 던진다.")
        void It_throws_exception_when_no_stock() {
            Ticket ticket = ticketRepository.save(Ticket.builder()
                    .name("Test Ticket")
                    .detail("Test Ticket Detail")
                    .price(10000L)
                    .quantity(1)
                    .startSaleTime(ticketSaleStartTime)
                    .endSaleTime(ticketSaleStartTime.plusDays(2))
                    .refundEndTime(ticketSaleStartTime.plusDays(2))
                    .festival(festival)
                    .build());
            TicketStock save = ticketStockRepository.save(ticket.createTicketStock());
            save.decreaseStock();
            ticketStockRepository.save(save);

            assertThatThrownBy(
                    () -> purchaseService.getPurchasePreviewInfo(purchaser.getId(), festival.getId(), ticket.getId()))
                    .isInstanceOf(ApiException.class)
                    .hasMessage(TicketErrorCode.TICKET_STOCK_EMPTY.getMessage());
        }
    }
}
