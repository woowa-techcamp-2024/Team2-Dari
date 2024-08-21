package com.wootecam.festivals.domain.purchase.service;

import static com.wootecam.festivals.utils.Fixture.createFestival;
import static com.wootecam.festivals.utils.Fixture.createMember;
import static com.wootecam.festivals.utils.Fixture.createTicket;
import static com.wootecam.festivals.utils.Fixture.createTicketStock;
import static org.junit.jupiter.api.Assertions.*;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TicketStockRollbackerTest extends SpringBootTestConfig {

    private final TicketStockRollbacker ticketStockRollbacker;
    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;
    private final MemberRepository memberRepository;
    private final FestivalRepository festivalRepository;

    @Autowired
    public TicketStockRollbackerTest(TicketStockRollbacker ticketStockRollbacker,
                                     TicketRepository ticketRepository,
                                     TicketStockRepository ticketStockRepository,
                                     MemberRepository memberRepository,
                                     FestivalRepository festivalRepository1) {
        this.ticketStockRollbacker = ticketStockRollbacker;
        this.ticketRepository = ticketRepository;
        this.ticketStockRepository = ticketStockRepository;
        this.memberRepository = memberRepository;
        this.festivalRepository = festivalRepository1;
    }

    @Nested
    @DisplayName("rollbackTicketStock 메소드는")
    class Describe_rollbackTicketStock {

        private Member member;
        private Festival festival;
        private Ticket ticket;
        private LocalDateTime ticketSaleStartTime = LocalDateTime.now();

        @BeforeEach
        void setUp() {
            clear();

            Member admin = memberRepository.save(createMember("admin", "admin@test.com"));
            festival = festivalRepository.save(createFestival(admin, "Test Festival", "Test Festival Detail",
                    ticketSaleStartTime.plusDays(1), ticketSaleStartTime.plusDays(4)));
            ticket = ticketRepository.save(createTicket(festival, 10000L, 100, ticketSaleStartTime, ticketSaleStartTime.plusDays(3)));
        }

        @Test
        @DisplayName("해당 티켓의 재고 정보를 복구한다")
        void rollbackTicketStock() {
            // Given
            Long ticketId = ticket.getId();
            int quantity = 1;
            ticketStockRepository.save(createTicketStock(ticket, 99));

            // When
            ticketStockRollbacker.rollbackTicketStock(ticketId, quantity);

            // Then
            assertEquals(100, ticketStockRepository.findByTicket(ticket).get().getRemainStock());
        }

        @Test
        @DisplayName("해당 티켓의 재고 정보가 존재하지 않을 때 IllegalArgumentException을 던진다")
        void throwsIllegalArgumentExceptionWhenTicketStockNotFound() {
            // Given
            Long ticketId = ticket.getId();
            int quantity = 1;

            // When, Then
            assertThrows(IllegalArgumentException.class, () -> ticketStockRollbacker.rollbackTicketStock(ticketId, quantity));
        }
    }
}