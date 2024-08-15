package com.wootecam.festivals.domain.ticket.dto;

import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_DETAIL_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_END_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_END_TIME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_NAME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_NAME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_PRICE_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_QUANTITY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_REFUND_TIME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_START_TIME_EMPTY_VALID_MESSAGE;
import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Nested
@DisplayName("TicketCreateRequest 클래스는")
class TicketCreateRequestTest {

    private final Validator validator = buildDefaultValidatorFactory().getValidator();

    private static Stream<Arguments> invalidTicketCreateDtos() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of(null, "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_NAME_EMPTY_VALID_MESSAGE),
                Arguments.of("", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_NAME_EMPTY_VALID_MESSAGE),
                Arguments.of("a".repeat(101), "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_NAME_VALID_MESSAGE),
                Arguments.of("티켓 이름", "a".repeat(1001), 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_DETAIL_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", null, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_PRICE_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", -1L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_PRICE_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000000000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_PRICE_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 0, now, now.plusDays(1), now.plusDays(1),
                        TICKET_QUANTITY_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100001, now, now.plusDays(1), now.plusDays(1),
                        TICKET_QUANTITY_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100, null, now.plusDays(1), now.plusDays(1),
                        TICKET_START_TIME_EMPTY_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100, now, null, now.plusDays(1),
                        TICKET_END_TIME_EMPTY_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100, now, now.minusDays(1), now.plusDays(1),
                        TICKET_END_TIME_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(1), null,
                        TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.minusDays(1),
                        TICKET_REFUND_TIME_VALID_MESSAGE)
        );
    }

    @Test
    @DisplayName("자기자신을 생성할 수 있다.")
    void create() {
        // Given
        String name = "티켓 이름";
        String detail = "티켓 상세";
        int quantity = 100;
        long price = 10000L;
        LocalDateTime startSaleTime = LocalDateTime.now();
        LocalDateTime endSaleTime = LocalDateTime.now().plusDays(1);
        LocalDateTime refundEndTime = LocalDateTime.now().plusDays(1);
        TicketCreateRequest request = new TicketCreateRequest(name, detail, price, quantity, startSaleTime, endSaleTime,
                refundEndTime);

        // Then
        assertAll(
                () -> assertEquals(name, request.name()),
                () -> assertEquals(detail, request.detail()),
                () -> assertEquals(price, request.price()),
                () -> assertEquals(quantity, request.quantity()),
                () -> assertEquals(startSaleTime, request.startSaleTime()),
                () -> assertEquals(endSaleTime, request.endSaleTime()),
                () -> assertEquals(refundEndTime, request.refundEndTime())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTicketCreateDtos")
    @DisplayName("잘못된 데이터로 DTO 생성 시 검증 실패")
    void invalidDtoShouldFail(String name, String detail, Long price, int quantity, LocalDateTime startSaleTime,
                              LocalDateTime endSaleTime, LocalDateTime refundEndTime, String expectedViolation) {
        // Given
        TicketCreateRequest createTicketRequest = new TicketCreateRequest(name, detail, price, quantity, startSaleTime,
                endSaleTime, refundEndTime);

        // When
        Set<ConstraintViolation<TicketCreateRequest>> violations = validator.validate(createTicketRequest);
        var violationEmpty = violations.isEmpty();
        var violationMessages = violations.stream().map(v -> v.getMessage()).toList();

        // Then
        assertAll(
                () -> assertEquals(false, violationEmpty),
                () -> assertEquals(true, violationMessages.contains(expectedViolation))
        );
    }
}
