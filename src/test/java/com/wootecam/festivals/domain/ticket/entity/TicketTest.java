package com.wootecam.festivals.domain.ticket.entity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.stub.FestivalStub;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TicketTest {

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
