package com.wootecam.festivals.domain.ticket.dto;

import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MAX_TICKET_NAME_LENGTH;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MAX_TICKET_PRICE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MAX_TICKET_QUANTITY;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MIN_TICKET_NAME_LENGTH;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MIN_TICKET_PRICE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.MIN_TICKET_QUANTITY;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_DETAIL_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_END_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_END_TIME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_NAME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_NAME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_PRICE_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_QUANTITY_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_QUANTITY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_REFUND_TIME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_START_TIME_EMPTY_VALID_MESSAGE;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * DTO for {@link com.wootecam.festivals.domain.ticket.entity.Ticket} 티켓 생성 요청 DTO
 */
public record TicketCreateRequest(@NotBlank(message = TICKET_NAME_EMPTY_VALID_MESSAGE)
                                  @Length(min = MIN_TICKET_NAME_LENGTH, max = MAX_TICKET_NAME_LENGTH, message = TICKET_NAME_VALID_MESSAGE)
                                  String name,
                                  @Size(max = 1000, message = TICKET_DETAIL_VALID_MESSAGE)
                                  String detail,
                                  @NotNull(message = TICKET_PRICE_VALID_MESSAGE)
                                  @Range(min = MIN_TICKET_PRICE, max = MAX_TICKET_PRICE, message = TICKET_PRICE_VALID_MESSAGE)
                                  Long price,
                                  @NotNull(message = TICKET_QUANTITY_EMPTY_VALID_MESSAGE)
                                  @Range(min = MIN_TICKET_QUANTITY, max = MAX_TICKET_QUANTITY, message = TICKET_QUANTITY_VALID_MESSAGE)
                                  Integer quantity,
                                  @NotNull(message = TICKET_START_TIME_EMPTY_VALID_MESSAGE)
                                  LocalDateTime startSaleTime,
                                  @NotNull(message = TICKET_END_TIME_EMPTY_VALID_MESSAGE)
                                  @Future(message = TICKET_END_TIME_VALID_MESSAGE)
                                  LocalDateTime endSaleTime,
                                  @NotNull(message = TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE)
                                  @Future(message = TICKET_REFUND_TIME_VALID_MESSAGE)
                                  LocalDateTime refundEndTime) {

    public Ticket toEntity(Festival festival) {
        return Ticket.builder()
                .festival(festival)
                .name(name)
                .detail(detail)
                .price(price)
                .quantity(quantity)
                .startSaleTime(startSaleTime)
                .endSaleTime(endSaleTime)
                .refundEndTime(refundEndTime)
                .build();
    }
}
