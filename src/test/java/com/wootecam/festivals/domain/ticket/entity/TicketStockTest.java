package com.wootecam.festivals.domain.ticket.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TicketStockTest {

    @Nested
    @DisplayName("decreaseStock는")
    class Describe_decreaseStock {

        Festival festival = FestivalStub.createFestivalWithTime(LocalDateTime.now(), LocalDateTime.now().plusDays(7));
        LocalDateTime now = LocalDateTime.now();
        Ticket ticket = new Ticket(festival, "티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1));

        @Nested
        @DisplayName("남은 재고가 1 이상일 때")
        class Context_with_remain_stock_more_than_one {

            TicketStock ticketStock = TicketStock.builder()
                    .remainStock(2)
                    .ticket(ticket)
                    .build();

            @DisplayName("재고를 차감하면 남은 재고가 1 감소한다")
            @Test
            void It_decreases_remain_stock_by_one() {
                ticketStock.decreaseStock();

                assertThat(ticketStock.getRemainStock()).isEqualTo(1);
            }
        }

        @Nested
        @DisplayName("남은 재고가 1일 때")
        class Context_with_remain_stock_is_one {

            TicketStock ticketStock = TicketStock.builder()
                    .remainStock(1)
                    .ticket(ticket)
                    .build();

            @DisplayName("재고를 차감하면 남은 재고가 0이 된다")
            @Test
            void It_decreases_remain_stock_to_zero() {
                ticketStock.decreaseStock();

                assertThat(ticketStock.getRemainStock()).isEqualTo(0);
            }
        }

        @Nested
        @DisplayName("남은 재고가 0일 때")
        class Context_with_remain_stock_is_zero {

            TicketStock ticketStock = TicketStock.builder()
                    .remainStock(0)
                    .ticket(ticket)
                    .build();

            @DisplayName("재고를 차감하면 예외가 발생한다")
            @Test
            void It_throws_exception() {
                assertThatThrownBy(ticketStock::decreaseStock)
                        .isInstanceOf(IllegalStateException.class);
            }
        }
    }
}