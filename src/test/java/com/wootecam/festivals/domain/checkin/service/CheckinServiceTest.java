package com.wootecam.festivals.domain.checkin.service;

import static com.wootecam.festivals.domain.checkin.exception.CheckinErrorCode.ALREADY_SAVED_CHECKIN;
import static com.wootecam.festivals.domain.checkin.exception.CheckinErrorCode.CHECKIN_NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import com.wootecam.festivals.domain.checkin.exception.CheckinErrorCode;
import com.wootecam.festivals.domain.checkin.repository.CheckinRepository;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("CheckinService 통합 테스트")
class CheckinServiceTest extends SpringBootTestConfig {

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    private Member member;
    private Festival festival;
    private Ticket ticket;

    @BeforeEach
    void setup() {
        TestDBCleaner.clear(checkinRepository);
        TestDBCleaner.clear(ticketRepository);
        TestDBCleaner.clear(festivalRepository);
        TestDBCleaner.clear(memberRepository);
        member = memberRepository.save(Member.builder()
                .name("test")
                .email("test@example.com")
                .profileImg("profile-img")
                .build()
        );

        festival = festivalRepository.save(Festival.builder()
                .admin(member)
                .title("페스티벌 이름")
                .description("페스티벌 설명")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(7))
                .build());

        ticket = ticketRepository.save(Ticket.builder()
                .name("Test Ticket")
                .detail("Test Ticket Detail")
                .price(10000L)
                .quantity(100)
                .startSaleTime(LocalDateTime.now())
                .endSaleTime(LocalDateTime.now().plusDays(2))
                .refundEndTime(LocalDateTime.now().plusDays(2))
                .festival(festival)
                .build());
    }

    // fk 이슈 때문에 테스트 종료 후 데이터 초기화
    @AfterEach
    void tearDown() {
        TestDBCleaner.clear(checkinRepository);
        TestDBCleaner.clear(ticketRepository);
        TestDBCleaner.clear(festivalRepository);
        TestDBCleaner.clear(memberRepository);
    }

    @Nested
    @DisplayName("saveCheckin 메서드는")
    class SaveCheckin {

        @Test
        @DisplayName("체크인 정보를 저장한다")
        void saveCheckin_Success() {
            // Given
            Long savedCheckinId = checkinService.createPendingCheckin(member.getId(), ticket.getId());

            // When & Then
            assertAll(
                    () -> assertNotNull(savedCheckinId),
                    () -> Assertions.assertThat(checkinRepository.findById(savedCheckinId)).isPresent()
                            .hasValueSatisfying(checkin -> {
                                assertAll(
                                        () -> assertEquals(member.getId(), checkin.getMember().getId()),
                                        () -> assertEquals(ticket.getFestival().getId(), checkin.getFestival().getId()),
                                        () -> assertEquals(ticket.getId(), checkin.getTicket().getId())
                                );
                            })
            );
        }

        @Test
        @DisplayName("이미 체크인했다면 예외를 던진다")
        void saveCheckin_AlreadyCheckedIn() {
            // Given 이미 체크인을 했다면
            checkinService.createPendingCheckin(member.getId(), ticket.getId());

            // When & Then
            assertThatThrownBy(() -> checkinService.createPendingCheckin(member.getId(), ticket.getId()))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ALREADY_SAVED_CHECKIN);
        }
    }

    @Nested
    @DisplayName("updateCheckedIn 메서드는")
    class UpdateCheckedin {

        Long savedCheckinId;

        @BeforeEach
        void setup() {
            // 체크인을 이미 한 상태
            savedCheckinId = checkinService.createPendingCheckin(member.getId(), ticket.getId());
        }

        @Test
        @DisplayName("체크인을 하면 isChecked = True 로 변경된다")
        void updateCheckedIn_Success() {
            // Given, When
            checkinService.completeCheckin(savedCheckinId);

            // Then
            Checkin checkin = checkinRepository.findById(savedCheckinId)
                    .orElseThrow(() -> new ApiException(CheckinErrorCode.CHECKIN_NOT_FOUND));

            assertTrue(checkin.isChecked());
        }

        @Test
        @DisplayName("체크인하지 않은 유저는 체크인을 할 수 없다")
        void updateCheckedIn_CheckinNotFound() {
            // Given
            Long notCheckedInCheckinId = 999L;

            // When & Then
            assertThatThrownBy(() ->
                    checkinService.completeCheckin(notCheckedInCheckinId))
                    .hasFieldOrPropertyWithValue("errorCode", CHECKIN_NOT_FOUND);
        }
    }
}