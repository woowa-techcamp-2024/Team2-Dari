package com.wootecam.festivals.domain.ticket.utils;

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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TicketValidatorTest {

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
    @DisplayName("티켓이 유효하지 않은 경우 예외를 던진다.")
    void invalidShouldFail(Festival festival,
                           String name, String detail,
                           Long price, int quantity,
                           LocalDateTime startSaleTime, LocalDateTime endSaleTime, LocalDateTime refundEndTime,
                           String validMessage) {

        assertThatThrownBy(
                () -> TicketValidator.validTicket(festival, name, detail, price, quantity, startSaleTime, endSaleTime,
                        refundEndTime))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(validMessage);
    }

    @Test
    @DisplayName("티켓이 유효한 경우 예외를 던지지 않는다.")
    void validShouldSuccess() {
        Festival festival = FestivalStub.createValidFestival(1L);
        LocalDateTime now = LocalDateTime.now();

        assertThatCode(
                () -> TicketValidator.validTicket(festival, "티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(2),
                        now.plusDays(1)))
                .doesNotThrowAnyException();
    }
}
