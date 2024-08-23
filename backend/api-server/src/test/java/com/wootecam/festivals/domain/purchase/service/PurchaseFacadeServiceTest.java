package com.wootecam.festivals.domain.purchase.service;

import static com.wootecam.festivals.utils.Fixture.createFestival;
import static com.wootecam.festivals.utils.Fixture.createMember;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.wootecam.festivals.domain.checkin.repository.CheckinRepository;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.dto.PurchaseTicketResponse;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockJdbcRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PurchaseFacadeServiceTest extends SpringBootTestConfig {

    private final PurchaseFacadeService purchaseFacadeService;
    private final MemberRepository memberRepository;
    private final FestivalRepository festivalRepository;
    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;
    private final PurchaseRepository purchaseRepository;
    private final CheckinRepository checkinRepository;

    private LocalDateTime ticketSaleStartTime = LocalDateTime.now();
    private Festival festival;
    private Member member;

    @Autowired
    public PurchaseFacadeServiceTest(PurchaseFacadeService purchaseFacadeService, MemberRepository memberRepository,
                                     FestivalRepository festivalRepository, TicketRepository ticketRepository,
                                     TicketStockRepository ticketStockRepository,
                                     PurchaseRepository purchaseRepository,
                                     CheckinRepository checkinRepository) {
        this.purchaseFacadeService = purchaseFacadeService;
        this.memberRepository = memberRepository;
        this.festivalRepository = festivalRepository;
        this.ticketRepository = ticketRepository;
        this.ticketStockRepository = ticketStockRepository;
        this.purchaseRepository = purchaseRepository;
        this.checkinRepository = checkinRepository;
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
        TicketStock ticketStock;

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
            ticketStock = TicketStock.builder()
                    .ticket(ticket)
                    .build();
            ticketStock.reserveTicket(member.getId());
            ticketStock = ticketStockRepository.save(ticketStock);
        }

        @Test
        @DisplayName("티켓을 구매할 수 있다.")
        void It_return_new_purchase() {
            PurchaseTicketResponse response = purchaseFacadeService.purchaseTicket(member.getId(),
                    festival.getId(), ticket.getId(), ticketStock.getId());

            assertAll(
                    () -> assertNotNull(response),
                    () -> assertThat(purchaseRepository.findById(response.purchaseId())).isPresent(),
                    () -> assertThat(
                            checkinRepository.findByMemberIdAndTicketId(member.getId(), ticket.getId())).isPresent()
            );
        }
    }
}
