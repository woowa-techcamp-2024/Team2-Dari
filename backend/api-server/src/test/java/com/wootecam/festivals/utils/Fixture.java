package com.wootecam.festivals.utils;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import java.time.LocalDateTime;

public final class Fixture {

    private Fixture() {
    }

    public static Member createMember(String name, String email) {
        return Member.builder()
                .name(name)
                .email(email)
                .profileImg("https://example.com/test.jpg")
                .build();
    }

    public static Festival createFestival(Member member, String title, String description,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        return Festival.builder()
                .admin(member)
                .title(title)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    public static Ticket createTicket(Festival festival, Long price, int quantity,
                                      LocalDateTime startSaleTime, LocalDateTime endSaleTime) {
        return Ticket.builder()
                .name("Test Ticket")
                .detail("Test Ticket Detail")
                .price(price)
                .quantity(quantity)
                .startSaleTime(startSaleTime)
                .endSaleTime(endSaleTime)
                .refundEndTime(endSaleTime)
                .festival(festival)
                .build();
    }

    public static TicketStock createTicketStock(Ticket ticket, int stock) {
        return TicketStock.builder()
                .ticket(ticket)
                .remainStock(stock)
                .build();
    }
}
