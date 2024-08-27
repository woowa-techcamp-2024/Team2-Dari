package com.wootecam.festivals.domain.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.Fixture;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("TicketCacheService 통합 테스트")
class TicketCacheServiceTest extends SpringBootTestConfig {

    @Autowired
    private TicketCacheService ticketCacheService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("실제 캐시 동작 테스트")
    class Describe_RealCacheOperation {

        private com.wootecam.festivals.domain.ticket.entity.Ticket savedTicket;

        @BeforeEach
        void setUp() {
            clear();
            ticketCacheService.clearCache();
            Member member = Fixture.createMember("test", "test@test.com");
            Member savedMember = memberRepository.save(member);
            Festival festival = Fixture.createFestival(savedMember, "title", "description", LocalDateTime.now(),
                    LocalDateTime.now().plusDays(3));
            Festival savedFestival = festivalRepository.save(festival);
            com.wootecam.festivals.domain.ticket.entity.Ticket ticket = Fixture.createTicket(savedFestival, 1000L, 100,
                    LocalDateTime.now().minusDays(3),
                    LocalDateTime.now().plusDays(2));
            savedTicket = ticketRepository.save(ticket);
        }

        @Test
        @DisplayName("티켓 정보를 가져오고 캐시에 저장한다")
        void fetchAndCacheTicketInfo() {
            // When
            Ticket cachedInfo = ticketCacheService.getTicket(savedTicket.getId());

            // Then
            assertThat(cachedInfo).isNotNull();
            assertThat(cachedInfo.getId()).isEqualTo(savedTicket.getId());

            // 캐시에서 다시 가져와서 확인
            Ticket cachedInfoAgain = ticketCacheService.getTicket(savedTicket.getId());
            assertThat(cachedInfoAgain).isEqualTo(cachedInfo);
        }

        @Test
        @DisplayName("캐시된 정보를 무효화한다")
        void invalidateTicketInfo() {
            // Given
            ticketCacheService.getTicket(savedTicket.getId());

            // When
            ticketCacheService.invalidateTicketCache(savedTicket.getId());

            // Then
            Ticket invalidatedInfo = ticketCacheService.getTicket(savedTicket.getId());
            assertThat(invalidatedInfo).isNotNull(); // 캐시가 무효화되었지만, 다시 로드되어야 함
            assertThat(invalidatedInfo.getId()).isEqualTo(savedTicket.getId());
        }

        @Test
        @DisplayName("존재하지 않는 티켓에 대해 예외를 발생시킨다")
        void throwExceptionForNonExistingTicket() {
            // When & Then
            assertThatThrownBy(() -> ticketCacheService.getTicket(9999L))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", TicketErrorCode.TICKET_NOT_FOUND);
        }
    }
}