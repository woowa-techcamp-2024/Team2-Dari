package com.wootecam.festivals.domain.ticket.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TicketStockTest {

    @Nested
    @DisplayName("sellTicketStock은")
    class Describe_sellTicketStock {

        LocalDateTime now = LocalDateTime.now();
        Festival festival = FestivalStub.createFestivalWithTime(now, LocalDateTime.now().plusDays(7));
        Ticket ticket = Ticket.builder()
                .festival(festival)
                .name("티켓 이름")
                .detail("티켓 상세")
                .price(10000L)
                .quantity(100)
                .startSaleTime(now.minusMinutes(1))
                .endSaleTime(now.plusDays(1))
                .refundEndTime(now.plusDays(1))
                .build();

        @Nested
        @DisplayName("남은 재고가 있을 때")
        class Context_with_remain_stock_more_than_one {

            TicketStock ticketStock = TicketStock.builder()
                    .ticket(ticket)
                    .build();

            @DisplayName("재고를 차감하면 티켓 재고는 예약(점유) 상태가 된다.")
            @Test
            void It_decreases_remain_stock_by_one() {
                ticketStock.reserveTicket(1L);

                assertThat(ticketStock.isReservation()).isEqualTo(true);
            }
        }

        @Nested
        @DisplayName("티켓 재고가 팔리면")
        class Context_with_no_remain_stock {

            TicketStock ticketStock;

            @BeforeEach
            void setUp() {
                ticketStock = TicketStock.builder()
                        .ticket(ticket)
                        .build();
            }

            @DisplayName("이미 점유된 재고를 사려고 하면 예외를 던진다.")
            @Test
            void It_throws_exception_when_sell_stock() {
                ticketStock.reserveTicket(100L);
                assertThatThrownBy(() -> ticketStock.reserveTicket(101L))
                        .isInstanceOf(IllegalStateException.class);
            }

            @DisplayName("해당 티켓 재고가 예약(점유)되지 않았다면 은 false가 된다.")
            @Test
            void It_not_sell_stock_is_reservation() {
                assertThat(ticketStock.isReservation()).isFalse();
            }
        }
    }
}
