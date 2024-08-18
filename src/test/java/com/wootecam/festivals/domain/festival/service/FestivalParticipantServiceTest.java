package com.wootecam.festivals.domain.festival.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import com.wootecam.festivals.domain.checkin.repository.CheckinRepository;
import com.wootecam.festivals.domain.festival.dto.ParticipantsPaginationResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.fixture.FestivalParticipantFixture;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@Nested
@DisplayName("FestivalParticipantService 클래스의")
@SpringBootTest
@ActiveProfiles("local")
class FestivalParticipantServiceTest extends SpringBootTestConfig {

    @Autowired
    private FestivalParticipantService festivalParticipantService;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    Festival festival;

    Member admin;

    @BeforeEach
    void setUp() {
        clear();
        List<Member> members = FestivalParticipantFixture.createMembers(100);
        admin = members.get(0);
        festival = FestivalParticipantFixture.createFestival(admin);
        List<Ticket> tickets = FestivalParticipantFixture.createTickets(10, festival);
        List<Purchase> purchases = FestivalParticipantFixture.createPurchases(members, tickets);
        List<Checkin> checkins = FestivalParticipantFixture.createCheckins(purchases);

        memberRepository.saveAll(members);
        festivalRepository.save(festival);
        ticketRepository.saveAll(tickets);
        purchaseRepository.saveAll(purchases);
        checkinRepository.saveAll(checkins);
    }

    @Nested
    @DisplayName("getParticipantListWithPagination 메소드는 현재 요청자의 아이디와 페스티벌 아이디와 페이지 정보를 받아")
    class Describe_getParticipantListWithPagination {

        @Test
        @DisplayName("페스티벌 참가자 리스트 페이지네이션 결과를 반환한다. - 0번째 페이지")
        void it_returns_participants_pagination_response_zero_page() {
            //given
            Pageable pageable = PageRequest.of(0, 10);

            //when
            ParticipantsPaginationResponse response = festivalParticipantService.getParticipantListWithPagination(
                    admin.getId(),
                    festival.getId(), pageable);

            //then
            assertAll(() -> assertNotNull(response),
                    () -> assertThat(response.participants()).hasSize(10),
                    () -> assertThat(response.totalItems()).isEqualTo(100),
                    () -> assertThat(response.totalPages()).isEqualTo(10),
                    () -> assertThat(response.currentPage()).isZero(),
                    () -> assertTrue(response.hasNext()),
                    () -> assertFalse(response.hasPrevious())
            );
        }

        @Test
        @DisplayName("페스티벌 참가자 리스트 페이지네이션 결과를 반환한다. - 4번째 페이지")
        void it_returns_participants_pagination_response_five_page() {
            //given
            Pageable pageable = PageRequest.of(4, 10);

            //when
            ParticipantsPaginationResponse response = festivalParticipantService.getParticipantListWithPagination(
                    admin.getId(),
                    festival.getId(), pageable);

            //then
            assertAll(() -> assertNotNull(response),
                    () -> assertThat(response.participants()).hasSize(10),
                    () -> assertThat(response.totalItems()).isEqualTo(100),
                    () -> assertThat(response.totalPages()).isEqualTo(10),
                    () -> assertThat(response.currentPage()).isEqualTo(4),
                    () -> assertTrue(response.hasNext()),
                    () -> assertTrue(response.hasPrevious())
            );
        }

        @Test
        @DisplayName("페스티벌 참가자 리스트 페이지네이션 결과를 반환한다. - 마지막(9)번째 페이지")
        void it_returns_participants_pagination_response_last_page() {
            //given
            Pageable pageable = PageRequest.of(9, 10);

            //when
            ParticipantsPaginationResponse response = festivalParticipantService.getParticipantListWithPagination(
                    admin.getId(),
                    festival.getId(), pageable);

            //then
            assertAll(() -> assertNotNull(response),
                    () -> assertThat(response.participants()).hasSize(10),
                    () -> assertThat(response.totalItems()).isEqualTo(100),
                    () -> assertThat(response.totalPages()).isEqualTo(10),
                    () -> assertThat(response.currentPage()).isEqualTo(9),
                    () -> assertFalse(response.hasNext()),
                    () -> assertTrue(response.hasPrevious())
            );
        }

        @Test
        @DisplayName("페스티벌 참가자 리스트 페이지네이션 결과를 반환한다. - 80개씩 요청하다가 마지막 페이지에선 20개만 반환된다.")
        void it_returns_participants_pagination_response_last_page_with_20_items() {
            //given
            Pageable pageable = PageRequest.of(1, 80);

            //when
            ParticipantsPaginationResponse response = festivalParticipantService.getParticipantListWithPagination(
                    admin.getId(),
                    festival.getId(), pageable);

            //then
            assertAll(() -> assertNotNull(response),
                    () -> assertThat(response.participants()).hasSize(20),
                    () -> assertThat(response.totalItems()).isEqualTo(100),
                    () -> assertThat(response.totalPages()).isEqualTo(2),
                    () -> assertThat(response.currentPage()).isEqualTo(1),
                    () -> assertFalse(response.hasNext()),
                    () -> assertTrue(response.hasPrevious())
            );
        }

        @Test
        @DisplayName("페스티벌이 없으면 예외를 던진다.")
        void it_throws_exception_when_festival_not_found() {
            //given
            Long invalidFestivalId = festival.getId() + 1;
            Pageable pageable = PageRequest.of(0, 10);

            //when, then
            assertThatThrownBy(
                    () -> festivalParticipantService.getParticipantListWithPagination(admin.getId(), invalidFestivalId,
                            pageable))
                    .isInstanceOf(ApiException.class)
                    .hasMessage(FestivalErrorCode.FESTIVAL_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("페스티벌의 어드민이 아닌 멤버가 조회 시 예외를 던진다.")
        void it_throws_exception_when_not_admin_member() {
            //given
            Long invalidAdminId = admin.getId() + 1;
            Pageable pageable = PageRequest.of(0, 10);

            //when, then
            assertThatThrownBy(
                    () -> festivalParticipantService.getParticipantListWithPagination(invalidAdminId, festival.getId(),
                            pageable))
                    .isInstanceOf(ApiException.class)
                    .hasMessage(FestivalErrorCode.FESTIVAL_NOT_AUTHORIZED.getMessage());
        }
    }
}
