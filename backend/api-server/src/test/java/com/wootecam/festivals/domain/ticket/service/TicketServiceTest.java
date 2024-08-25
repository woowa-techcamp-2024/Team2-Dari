package com.wootecam.festivals.domain.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.dto.TicketIdResponse;
import com.wootecam.festivals.domain.ticket.dto.TicketListResponse;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TicketServiceTest extends SpringBootTestConfig {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TicketStockRepository ticketStockRepository;

    @BeforeEach
    void setUp() {
        clear();
    }

    @Nested
    @DisplayName("createTicket 메소드는")
    class Describe_createTicket {

        @Test
        @DisplayName("티켓 생성에 성공하면 생성된 티켓의 id를 반환한다.")
        void it_returns_ticket_id_when_ticket_is_created() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            Member admin = Member.builder()
                    .name("관리자")
                    .profileImg("기관 이미지")
                    .email("eamil@emai.com")
                    .build();

            Festival festival = Festival.builder()
                    .admin(admin)
                    .title("페스티벌 이름")
                    .description("페스티벌 설명")
                    .startTime(now)
                    .endTime(now.plusDays(7))
                    .build();
            memberRepository.save(admin);
            Festival saveFestival = festivalRepository.save(festival);

            TicketCreateRequest ticketCreateRequest = new TicketCreateRequest("티켓 이름", "티켓 설명", 10000L, 100,
                    now.minusMinutes(1), now.plusDays(6), now.plusDays(10));

            // When
            TicketIdResponse ticketIdResponse = ticketService.createTicket(saveFestival.getId(), ticketCreateRequest);
            Optional<Ticket> findTicket = ticketRepository.findById(ticketIdResponse.ticketId());
            // Then
            assertAll(
                    () -> assertThat(ticketIdResponse).isNotNull(),
                    () -> assertThat(findTicket.isPresent()).isTrue(),
                    () -> assertThat(ticketStockRepository.findAll()).hasSize(findTicket.get().getQuantity())
            );
        }

        @Test
        @DisplayName("페스티벌을 찾을 수 없으면 예외를 던진다.")
        void it_throws_festival_not_found_exception_when_festival_is_not_found() {
            // Given
            Long festivalId = 1L;
            TicketCreateRequest ticketCreateRequest = new TicketCreateRequest("티켓 이름", "티켓 설명", 10000L, 100,
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(10));

            // When, Then
            assertThatThrownBy(() -> ticketService.createTicket(festivalId, ticketCreateRequest))
                    .isInstanceOf(ApiException.class);
        }
    }

    @Nested
    @DisplayName("getTickets 메소드는")
    class Describe_getTickets {

        @Test
        @DisplayName("페스티벌의 티켓 목록을 반환한다.")
        void it_returns_ticket_list_of_festival() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            Member admin = Member.builder()
                    .name("관리자")
                    .profileImg("기관 이미지")
                    .email("eamil@emai.com")
                    .build();

            Festival festival = Festival.builder()
                    .admin(admin)
                    .title("페스티벌 이름")
                    .description("페스티벌 설명")
                    .startTime(now)
                    .endTime(now.plusDays(7))
                    .build();

            memberRepository.save(admin);
            Festival saveFestival = festivalRepository.save(festival);

            for (int i = 0; i < 5; i++) {
                Ticket ticket = Ticket.builder()
                        .festival(saveFestival)
                        .name("티켓 이름" + i)
                        .detail("티켓 설명" + i)
                        .price(10000L)
                        .quantity(100)
                        .startSaleTime(now.minusMinutes(1))
                        .endSaleTime(now.plusDays(6))
                        .refundEndTime(now.plusDays(10))
                        .build();

                TicketStock ticketStock = TicketStock.builder()
                        .ticket(ticket)
                        .build();

                ticketRepository.save(ticket);
                ticketStockRepository.save(ticketStock);
            }

            // When
            TicketListResponse ticketListResponse = ticketService.getTickets(saveFestival.getId());

            // Then
            assertAll(
                    () -> assertThat(ticketListResponse).isNotNull(),
                    () -> assertThat(ticketListResponse.tickets()).hasSize(5)
            );
        }

        @Test
        @DisplayName("페스티벌을 찾을 수 없으면 예외를 던진다.")
        void it_throws_festival_not_found_exception_when_festival_is_not_found() {
            // Given
            Long festivalId = 1L;

            // When, Then
            assertThatThrownBy(() -> ticketService.getTickets(festivalId))
                    .isInstanceOf(ApiException.class);
        }
    }
}
