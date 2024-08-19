package com.wootecam.festivals.domain.festival.fixture;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FestivalParticipantFixture {

    private FestivalParticipantFixture() {
    }

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

    public static Festival createFestival(Member admin) {
        LocalDateTime now = LocalDateTime.now();

        return Festival.builder()
                .admin(admin)
                .title("테스트 축제")
                .description("축제 설명")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(7))
                .build();
    }

    public static List<Ticket> createTickets(int count, Festival festival) {
        List<Ticket> tickets = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Ticket ticket = Ticket.builder()
                    .festival(festival)
                    .name("ticket" + i)
                    .detail("ticket detail" + i)
                    .price(i * 1000L)
                    .quantity(i * 10)
                    .startSaleTime(festival.getStartTime().minusDays(1))
                    .endSaleTime(festival.getStartTime().plusDays(3))
                    .refundEndTime(festival.getEndTime().minusDays(1))
                    .build();

            tickets.add(ticket);
        }

        return tickets;
    }

    public static List<Purchase> createPurchases(List<Member> members, List<Ticket> tickets) {
        List<Purchase> purchases = new ArrayList<>();

        for (int i = 0; i < members.size(); i++) {
            int ticketIndex = i % tickets.size();
            Purchase purchase = Purchase.builder()
                    .member(members.get(i))
                    .ticket(tickets.get(ticketIndex))
                    .purchaseTime(tickets.get(ticketIndex).getStartSaleTime().plusDays(1))
                    .purchaseStatus(PurchaseStatus.PURCHASED)
                    .build();

            purchases.add(purchase);
        }

        return purchases;
    }

    public static List<Checkin> createCheckins(List<Purchase> purchases) {
        List<Checkin> checkins = new ArrayList<>();

        for (int i = 0; i < purchases.size(); i++) {
            Checkin checkin = Checkin.builder()
                    .member(purchases.get(i).getMember())
                    .ticket(purchases.get(i).getTicket())
                    .build();

            checkins.add(checkin);
        }

        return checkins;
    }
}
