package com.wootecam.festivals.domain.ticket.repository;

import static com.wootecam.festivals.utils.Fixture.createFestival;
import static com.wootecam.festivals.utils.Fixture.createMember;
import static org.assertj.core.api.Assertions.assertThat;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TicketStockJdbcRepositoryTest extends SpringBootTestConfig {

    private final TicketStockJdbcRepository ticketStockJdbcRepository;
    private final MemberRepository memberRepository;
    private final FestivalRepository festivalRepository;
    private final TicketRepository ticketRepository;
    private final TicketStockRepository ticketStockRepository;

    private Festival festival;
    private Member admin;
    private LocalDateTime ticketSaleStartTime = LocalDateTime.now();
    @Autowired
    TicketStockJdbcRepositoryTest(TicketStockJdbcRepository ticketStockJdbcRepository,
                                  MemberRepository memberRepository, FestivalRepository festivalRepository,
                                  TicketRepository ticketRepository, TicketStockRepository ticketStockRepository) {
        this.ticketStockJdbcRepository = ticketStockJdbcRepository;
        this.memberRepository = memberRepository;
        this.festivalRepository = festivalRepository;
        this.ticketRepository = ticketRepository;
        this.ticketStockRepository = ticketStockRepository;
    }

    @BeforeEach
    void setUp() {
        clear();

        admin = memberRepository.save(createMember("admin", "admin@test.com"));
        festival = festivalRepository.save(createFestival(admin, "Test Festival", "Test Festival Detail",
                ticketSaleStartTime.plusDays(1), ticketSaleStartTime.plusDays(4)));
    }

    @Test
    @DisplayName("티켓 재고 배치 저장")
    void saveTicketStocks() {
        // given
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

        Ticket save = ticketRepository.save(ticket);
        // when
        ticketStockJdbcRepository.saveTicketStocks(save.createTicketStock());

        // then
        List<TicketStock> ticketStocks = ticketStockRepository.findAll();

        assertThat(ticketStocks).hasSize(100);
    }

}
