package com.wootecam.festivals.domain.my.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.my.dto.MyFestivalCursor;
import com.wootecam.festivals.domain.my.dto.MyFestivalResponse;
import com.wootecam.festivals.domain.my.dto.MyPurchasedTicketResponse;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.constants.GlobalConstants;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.page.CursorBasedPage;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("MyService 통합 테스트")
class MyServiceTest extends SpringBootTestConfig {

    private final MyService myService;
    private final FestivalRepository festivalRepository;
    private final MemberRepository memberRepository;
    private final TicketRepository ticketRepository;
    private final PurchaseRepository purchaseRepository;

    private Member admin;

    @Autowired
    public MyServiceTest(MyService myService, FestivalRepository festivalRepository,
                         MemberRepository memberRepository, TicketRepository ticketRepository,
                         PurchaseRepository purchaseRepository) {
        this.myService = myService;
        this.festivalRepository = festivalRepository;
        this.memberRepository = memberRepository;
        this.ticketRepository = ticketRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @BeforeEach
    void setUp() {
        clear();

        admin = memberRepository.save(
                Member.builder()
                        .name("Test Admin")
                        .email("Test Detail")
                        .profileImg("Test profileImg")
                        .build());
    }

    @Nested
    @DisplayName("내가 주최한 축제 목록 요청 시")
    class Describe_findHostedFestival {

        @Test
        @DisplayName("커서가 없다면 사용자가 개최한 축제 목록의 첫 페이지를 반환한다.")
        void it_returns_my_festival_list_first_page() {
            // Given
            Long loginMemberId = admin.getId();
            int count = 15;
            List<Festival> festivals = createFestivals(count);

            // When
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> firstPage = myService.findHostedFestival(
                    loginMemberId, null, GlobalConstants.MIN_PAGE_SIZE);

            // Then
            assertAll(
                    () -> assertThat(firstPage.getContent()).hasSize(10),
                    () -> assertThat(firstPage.getCursor()).isNotNull(),
                    () -> assertThat(firstPage.hasNext()).isTrue(),
                    () -> assertThat(firstPage.getContent().get(0).festivalId()).isEqualTo(
                            festivals.get(count - 1).getId()),
                    () -> assertThat(firstPage.getContent().get(9).festivalId()).isEqualTo(
                            festivals.get(count - 10).getId())
            );
        }

        @Test
        @DisplayName("커서가 있다면 사용자가 개최한 축제 목록 중 커서의 다음 페이지를 반환한다.")
        void it_returns_my_festival_list_next_page() {
            // Given
            Long loginMemberId = admin.getId();
            int count = 25;
            List<Festival> festivals = createFestivals(count);
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> firstPage = myService.findHostedFestival(
                    loginMemberId, null, GlobalConstants.MIN_PAGE_SIZE);
            MyFestivalCursor cursor = firstPage.getCursor();

            // When
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> secondPage = myService.findHostedFestival(
                    loginMemberId, cursor, GlobalConstants.MIN_PAGE_SIZE);

            // Then
            assertAll(
                    () -> assertThat(secondPage.getContent()).hasSize(10),
                    () -> assertThat(secondPage.getCursor()).isNotNull(),
                    () -> assertThat(secondPage.hasNext()).isTrue(),
                    () -> assertThat(secondPage.getContent().get(0).festivalId()).isEqualTo(
                            festivals.get(count - 11).getId()),
                    () -> assertThat(secondPage.getContent().get(9).festivalId()).isEqualTo(
                            festivals.get(count - 20).getId())
            );
        }

        @Test
        @DisplayName("개최한 축제가 없다면 빈 리스트와 null 커서를 반환한다.")
        void it_returns_empty_list_and_null_cursor_for_empty_result() {
            // Given
            Long loginMemberId = admin.getId();

            // When
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> response = myService.findHostedFestival(loginMemberId,
                    null, GlobalConstants.MIN_PAGE_SIZE);

            // Then
            assertAll(
                    () -> assertThat(response.getContent()).isEmpty(),
                    () -> assertThat(response.getCursor()).isNull(),
                    () -> assertThat(response.hasNext()).isFalse()
            );
        }

        @Test
        @DisplayName("페이지 크기가 전체 결과보다 크다면 모든 결과를 반환하고 다음 페이지가 없음을 표시한다.")
        void it_returns_all_results_when_page_size_is_larger() {
            // Given
            Long loginMemberId = admin.getId();
            int count = 5;
            List<Festival> festivals = createFestivals(count);

            // When
            CursorBasedPage<MyFestivalResponse, MyFestivalCursor> response = myService.findHostedFestival(loginMemberId,
                    null, GlobalConstants.MIN_PAGE_SIZE);

            // Then
            assertAll(
                    () -> assertThat(response.getContent()).hasSize(count),
                    () -> assertThat(response.getCursor()).isNull(),
                    () -> assertThat(response.hasNext()).isFalse(),
                    () -> assertThat(response.getContent().get(0).festivalId()).isEqualTo(
                            festivals.get(count - 1).getId()),
                    () -> assertThat(response.getContent().get(4).festivalId()).isEqualTo(
                            festivals.get(count - 5).getId())
            );
        }

        private List<Festival> createFestivals(int count) {
            LocalDateTime now = LocalDateTime.now();
            return IntStream.range(1, count + 1)
                    .mapToObj(i -> Festival.builder()
                            .admin(admin)
                            .title("페스티벌 " + i)
                            .description("페스티벌 설명 " + i)
                            .startTime(now.plusDays(i + 1))
                            .endTime(now.plusDays(i + 8))
                            .festivalPublicationStatus(FestivalPublicationStatus.PUBLISHED)
                            .build()
                    )
                    .map(festivalRepository::save)
                    .toList();
        }
    }

    @Nested
    @DisplayName("내가 구매한 티켓 단건 요청 시")
    class Describe_findMyPurchasedTickets {

        private Festival festival;
        private Ticket ticket;
        private Purchase purchase;
        private Member loginMember;

        @BeforeEach
        void setup() {
            loginMember = createMember("loginMember");
            festival = createFestival(loginMember);
            ticket = createTicket(festival);
        }

        @Test
        @DisplayName("사용자가 구매한 티켓 목록을 반환한다.")
        void it_returns_my_purchased_ticket_list() {
            // When
            purchase = createPurchase(ticket);

            System.out.println("구매");
            List<Purchase> all = purchaseRepository.findAll();
            for (Purchase purchase : all) {
                System.out.println(purchase.getTicket().getId() + " " + purchase.getMember().getId());
            }

            System.out.println("loginMember.getId() : " + loginMember.getId());
            System.out.println("ticket.getId() : " + ticket.getId());

            MyPurchasedTicketResponse myPurchasedTicket = myService.findMyPurchasedTicket(loginMember.getId(), ticket.getId());

            // Then
            assertAll(
                    () -> assertThat(myPurchasedTicket.purchaseId()).isEqualTo(purchase.getId()),
                    () -> assertThat(myPurchasedTicket.purchaseTime()).isEqualTo(purchase.getPurchaseTime()),
                    () -> assertThat(myPurchasedTicket.purchaseStatus()).isEqualTo(purchase.getPurchaseStatus()),
                    () -> assertThat(myPurchasedTicket.ticket().id()).isEqualTo(ticket.getId()),
                    () -> assertThat(myPurchasedTicket.ticket().name()).isEqualTo(ticket.getName()),
                    () -> assertThat(myPurchasedTicket.ticket().detail()).isEqualTo(ticket.getDetail()),
                    () -> assertThat(myPurchasedTicket.ticket().price()).isEqualTo(ticket.getPrice()),
                    () -> assertThat(myPurchasedTicket.ticket().quantity()).isEqualTo(ticket.getQuantity()),
                    () -> assertThat(myPurchasedTicket.ticket().startSaleTime()).isEqualTo(ticket.getStartSaleTime()),
                    () -> assertThat(myPurchasedTicket.ticket().endSaleTime()).isEqualTo(ticket.getEndSaleTime()),
                    () -> assertThat(myPurchasedTicket.ticket().refundEndTime()).isEqualTo(ticket.getRefundEndTime()),
                    () -> assertThat(myPurchasedTicket.festival().festivalId()).isEqualTo(festival.getId()),
                    () -> assertThat(myPurchasedTicket.festival().adminId()).isEqualTo(festival.getAdmin().getId()),
                    () -> assertThat(myPurchasedTicket.festival().title()).isEqualTo(festival.getTitle()),
                    () -> assertThat(myPurchasedTicket.festival().description()).isEqualTo(festival.getDescription()),
                    () -> assertThat(myPurchasedTicket.festival().startTime()).isEqualTo(festival.getStartTime()),
                    () -> assertThat(myPurchasedTicket.festival().endTime()).isEqualTo(festival.getEndTime()),
                    () -> assertThat(myPurchasedTicket.festival().festivalPublicationStatus()).isEqualTo(
                            festival.getFestivalPublicationStatus()),
                    () -> assertThat(myPurchasedTicket.festival().festivalProgressStatus()).isEqualTo(
                            festival.getFestivalProgressStatus())
            );
        }

        @Test
        @DisplayName("사용자가 구매한 티켓이 없다면 예외를 던진다")
        void it_returns_empty_list_for_empty_result() {
            // When
            assertThatThrownBy(() -> myService.findMyPurchasedTicket(loginMember.getId(), ticket.getId()))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", PurchaseErrorCode.PURCHASE_NOT_FOUND);
        }

        private Festival createFestival(Member admin) {
            LocalDateTime now = LocalDateTime.now();
            return festivalRepository.save(
                    Festival.builder()
                            .admin(admin)
                            .title("페스티벌")
                            .description("페스티벌 설명")
                            .startTime(now.plusDays(1))
                            .endTime(now.plusDays(8))
                            .festivalPublicationStatus(FestivalPublicationStatus.PUBLISHED)
                            .build()
            );
        }

        // 페스티벌 하나당 한 개의 티켓을 가지는 것으로 가정
        private Ticket createTicket(Festival festival) {
            return ticketRepository.save(
                    Ticket.builder()
                            .festival(festival)
                            .name("티켓")
                            .detail("티켓 설명")
                            .price(10000L)
                            .quantity(100)
                            .startSaleTime(festival.getStartTime().minusHours(2))
                            .endSaleTime(festival.getEndTime().minusHours(1))
                            .refundEndTime(festival.getEndTime().minusHours(1))
                            .build()
            );
        }

        private Purchase createPurchase(Ticket ticket) {
            return purchaseRepository.save(
                    Purchase.builder()
                            .ticket(ticket)
                            .member(loginMember)
                            .purchaseTime(LocalDateTime.now())
                            .purchaseStatus(PurchaseStatus.PURCHASED)
                            .build()
            );
        }

        private Member createMember(String name) {
            return memberRepository.save(Member.builder()
                    .name(name)
                    .email(name + "@test.com")
                    .profileImg("profileImg")
                    .build());
        }
    }
}