package com.wootecam.festivals.domain.ticket.utils;

import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MAX_TICKET_DETAIL_LENGTH;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MAX_TICKET_NAME_LENGTH;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MAX_TICKET_PRICE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MAX_TICKET_QUANTITY;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MIN_TICKET_PRICE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MIN_TICKET_QUANTITY;
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
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_TIME_VALID_MESSAGE;

import com.wootecam.festivals.domain.festival.entity.Festival;
import java.time.LocalDateTime;
import java.util.Objects;

public class TicketValidator {

    private TicketValidator() {
    }

    /**
     * 티켓 생성 시 유효성 검사
     *
     * @param festival
     * @param name
     * @param detail
     * @param price
     * @param quantity
     * @param startSaleTime
     * @param endSaleTime
     * @param refundEndTime
     */
    public static void validTicket(Festival festival,
                                   String name, String detail,
                                   Long price, int quantity,
                                   LocalDateTime startSaleTime, LocalDateTime endSaleTime,
                                   LocalDateTime refundEndTime) {
        validateFestival(festival);
        validateName(name);
        validateDetail(detail);
        validatePrice(price);
        validateQuantity(quantity);
        validateTime(festival, startSaleTime, endSaleTime, refundEndTime);
    }

    private static void validateFestival(Festival festival) {
        Objects.requireNonNull(festival, TICKET_FESTIVAL_VALID_MESSAGE);
    }

    private static void validateName(String name) throws IllegalArgumentException {
        if (name == null || name.isEmpty() || name.length() > MAX_TICKET_NAME_LENGTH) {
            throw new IllegalArgumentException(TICKET_NAME_VALID_MESSAGE);
        }
    }

    private static void validateDetail(String detail) {
        if (detail.length() > MAX_TICKET_DETAIL_LENGTH) {
            throw new IllegalArgumentException(TICKET_DETAIL_VALID_MESSAGE);
        }
    }

    private static void validatePrice(Long price) {
        if (price == null || price < MIN_TICKET_PRICE || price > MAX_TICKET_PRICE) {
            throw new IllegalArgumentException(TICKET_PRICE_VALID_MESSAGE);
        }
    }

    private static void validateQuantity(int quantity) {
        if (quantity < MIN_TICKET_QUANTITY || quantity > MAX_TICKET_QUANTITY) {
            throw new IllegalArgumentException(TICKET_QUANTITY_VALID_MESSAGE);
        }
    }

    /**
     * 티켓 판매 시간 유효성 검사
     * 판매 시작 시간은 현재 시간 이후여야 하고, 판매 종료 시간은 현재 시간 이후 이벤트 종료 시간 이전이어야 하며,
     * 환불 종료 시간은 현재 시간 이후여야 한다.
     * @param festival
     * @param startSaleTime
     * @param endSaleTime
     * @param refundEndTime
     */
    private static void validateTime(Festival festival, LocalDateTime startSaleTime, LocalDateTime endSaleTime,
                                     LocalDateTime refundEndTime) {
        Objects.requireNonNull(startSaleTime, TICKET_START_TIME_EMPTY_VALID_MESSAGE);
        Objects.requireNonNull(endSaleTime, TICKET_END_TIME_EMPTY_VALID_MESSAGE);
        Objects.requireNonNull(refundEndTime, TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE);

        if (startSaleTime.isAfter(endSaleTime)) {
            throw new IllegalArgumentException(TICKET_TIME_VALID_MESSAGE);
        }

        if (startSaleTime.isAfter(festival.getStartTime())) {
            throw new IllegalArgumentException(TICKET_START_TIME_VALID_MESSAGE);
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(endSaleTime) || festival.getEndTime().isBefore(endSaleTime)) {
            throw new IllegalArgumentException(TICKET_END_TIME_VALID_MESSAGE);
        }

        if (now.isAfter(refundEndTime)) {
            throw new IllegalArgumentException(TICKET_REFUND_TIME_VALID_MESSAGE);
        }
    }
}
