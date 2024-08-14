package com.wootecam.festivals.domain.ticket.entity;

import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_DETAIL_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_END_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_END_TIME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_FESTIVAL_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_NAME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_PRICE_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_QUANTITY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_REFUND_TIME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_START_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_START_TIME_VALID_MESSAGE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TicketTest {

    private static Stream<Arguments> invalidTicket() {
        Festival festival = FestivalStub.createFestivalWithTime(LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of(null, "티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_FESTIVAL_VALID_MESSAGE),
                Arguments.of(festival, null, "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_NAME_VALID_MESSAGE),
                Arguments.of(festival, "", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_NAME_VALID_MESSAGE),
                Arguments.of(festival, "a".repeat(101), "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_NAME_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "a".repeat(1001), 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_DETAIL_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", null, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_PRICE_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", -1L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_PRICE_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000000000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_PRICE_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000L, 0, now, now.plusDays(1), now.plusDays(1),
                        TICKET_QUANTITY_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000L, 100001, now, now.plusDays(1), now.plusDays(1),
                        TICKET_QUANTITY_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000L, 100, null, now.plusDays(1), now.plusDays(1),
                        TICKET_START_TIME_EMPTY_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000L, 100, now.minusDays(1), now.plusDays(1),
                        now.plusDays(1),
                        TICKET_START_TIME_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000L, 100, now, null, now.plusDays(1),
                        TICKET_END_TIME_EMPTY_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000L, 100, festival.getEndTime().plusDays(1),
                        festival.getEndTime().plusDays(2), festival.getEndTime().plusDays(3),
                        TICKET_END_TIME_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(1), null,
                        TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE),
                Arguments.of(festival, "티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.minusDays(1),
                        TICKET_REFUND_TIME_VALID_MESSAGE)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTicket")
    @DisplayName("티켓 생성에 실패한다.")
    void validate(Festival festival, String name, String detail, Long price, int quantity, LocalDateTime startTime,
                  LocalDateTime endTime, LocalDateTime refundEndTime, String message) {
        assertThatThrownBy(() -> new Ticket(festival, name, detail, price, quantity, startTime, endTime, refundEndTime))
                .hasMessage(message);
    }

    @Test
    @DisplayName("티켓 생성에 성공한다.")
    void createTicket() {
        Festival festival = FestivalStub.createFestivalWithTime(LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = new Ticket(festival, "티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1));
        assertAll(
                () -> assertEquals(festival, ticket.getFestival()),
                () -> assertEquals("티켓 이름", ticket.getName()),
                () -> assertEquals("티켓 상세", ticket.getDetail()),
                () -> assertEquals(10000L, ticket.getPrice()),
                () -> assertEquals(100, ticket.getQuantity()),
                () -> assertEquals(now, ticket.getStartSaleTime()),
                () -> assertEquals(now.plusDays(1), ticket.getEndSaleTime()),
                () -> assertEquals(now.plusDays(1), ticket.getRefundEndTime())
        );
    }

}
