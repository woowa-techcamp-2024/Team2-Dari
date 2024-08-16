package com.wootecam.festivals.domain.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(ticketRepository);
        TestDBCleaner.clear(festivalRepository);
    }

    @Nested
    @DisplayName("createTicket 메소드는")
    class Describe_createTicket {

        @Test
        @DisplayName("티켓 생성에 성공하면 생성된 티켓의 id를 반환한다.")
        void it_returns_ticket_id_when_ticket_is_created() {
            // Given
            LocalDateTime now = LocalDateTime.now();

            Festival festival = Festival.builder()
                    .title("페스티벌 이름")
                    .description("페스티벌 설명")
                    .startTime(now)
                    .endTime(now.plusDays(7))
                    .build();

            TicketCreateRequest ticketCreateRequest = new TicketCreateRequest("티켓 이름", "티켓 설명", 10000L, 100,
                    now.plusDays(1), now.plusDays(6), now.plusDays(10));

            // When
            Festival saveFestival = festivalRepository.save(festival);

            Festival findFestival = festivalRepository.findById(saveFestival.getId()).get();

            // Then
            assertAll(
                    () -> assertThat(ticketService.createTicket(saveFestival.getId(), ticketCreateRequest)).isNotNull(),
                    () -> assertThat(ticketRepository.findAll()).hasSize(1),
                    () -> assertThat(findFestival.getId()).isEqualTo(saveFestival.getId())
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
}
