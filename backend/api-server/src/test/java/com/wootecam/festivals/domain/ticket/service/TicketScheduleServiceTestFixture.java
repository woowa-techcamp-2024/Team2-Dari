package com.wootecam.festivals.domain.ticket.service;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketScheduleServiceTestFixture {

    private TicketScheduleServiceTestFixture() {
    }

    private static final LocalDateTime now = LocalDateTime.now();

    public static List<Member> createMembers(int count) {
        List<Member> members = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Member member = Member.builder()
                    .name("name" + i)
                    .email("email" + i)
                    .profileImg("profileImg" + i)
                    .build();

            members.add(member);
        }

        return members;
    }

    public static Festival createUpcomingFestival(Member admin) {
        return Festival.builder()
                .admin(admin)
                .title("테스트 축제")
                .description("축제 설명")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(100))
                .build();
    }

    /**
     * 10분 이내에 판매 시작하는 티켓 생성
     */
    public static List<Ticket> createSaleUpcomingTicketsWithinTenMinutes(int count, Festival festival) {
        return createTickets(count, festival, now.plusMinutes(5), now.plusDays(1));
    }

    /**
     * 10분 이후에 판매 시작하는 티켓 생성
     */
    public static List<Ticket> createSaleUpcomingTicketsAfterTenMinutes(int count, Festival festival) {
        return createTickets(count, festival, now.plusMinutes(15), now.plusDays(1));
    }

    /**
     * 판매 중인 티켓 생성
     */
    public static List<Ticket> createSaleOngoingTickets(int count, Festival festival) {
        return createTickets(count, festival, now.minusDays(1), now.plusDays(1));
    }

    /**
     * 판매 종료된 티켓 생성
     */
    public static List<Ticket> createSaleCompletedTickets(int count, Festival festival) {
        return createTickets(count, festival, now.minusDays(2), now.minusDays(1));
    }

    private static List<Ticket> createTickets(int count, Festival festival, LocalDateTime startSaleTime, LocalDateTime endSaleTime) {
        List<Ticket> tickets = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Ticket ticket = Ticket.builder()
                    .festival(festival)
                    .name("ticket" + i)
                    .detail("ticket detail" + i)
                    .price(i * 1000L)
                    .quantity(i * 10)
                    .startSaleTime(startSaleTime)
                    .endSaleTime(endSaleTime)
                    .refundEndTime(festival.getEndTime().minusDays(1))
                    .build();

            tickets.add(ticket);
        }

        return tickets;
    }
}
