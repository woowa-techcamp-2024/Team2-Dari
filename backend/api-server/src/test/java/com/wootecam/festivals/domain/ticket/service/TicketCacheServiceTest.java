package com.wootecam.festivals.domain.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
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
import org.springframework.boot.test.mock.mockito.SpyBean;

@DisplayName("TicketCacheService 통합 테스트")
class TicketCacheServiceTest extends SpringBootTestConfig {

    @Autowired
    private TicketCacheService ticketCacheService;
    @SpyBean
    private TicketRepository ticketRepository;
    @Autowired
    private FestivalRepository festivalRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Ticket savedTicket;

    @BeforeEach
    void setUp() {
        clear();
        ticketCacheService.clearCache();
        Member member = memberRepository.save(Fixture.createMember("test", "test@test.com"));
        Festival festival = festivalRepository.save(
                Fixture.createFestival(member, "title", "description", LocalDateTime.now().plusDays(3),
                        LocalDateTime.now().plusDays(5)));
        savedTicket = ticketRepository.save(Fixture.createTicket(festival, 1000L, 100, LocalDateTime.now().minusDays(3),
                LocalDateTime.now().plusDays(2)));
    }

    @Test
    @DisplayName("캐시 만료 시간을 테스트한다")
    void testCacheExpiration() throws InterruptedException {
        // Given
        ticketCacheService.getTicket(savedTicket.getId());

        // When
        Thread.sleep(2000); // 2초 대기 (실제 환경에서는 더 긴 시간이 필요할 수 있음)

        // Then
        Ticket cachedTicket = ticketCacheService.getTicket(savedTicket.getId());
        assertThat(cachedTicket).isNotNull();
        verify(ticketRepository, times(1)).findById(savedTicket.getId()); // 캐시가 만료되지 않았으므로 repository 호출은 1번만 이루어져야 함
    }

    @Nested
    @DisplayName("getTicket 메소드는")
    class Describe_getTicket {

        @Test
        @DisplayName("티켓 정보를 가져오고 캐시에 저장한다")
        void fetchAndCacheTicketInfo() {
            // When
            Ticket cachedTicket = ticketCacheService.getTicket(savedTicket.getId());

            // Then
            assertThat(cachedTicket).isNotNull();
            assertThat(cachedTicket.getId()).isEqualTo(savedTicket.getId());

            // 캐시에서 다시 가져와서 확인
            Ticket cachedTicketAgain = ticketCacheService.getTicket(savedTicket.getId());
            assertThat(cachedTicketAgain).isEqualTo(cachedTicket);

            // Repository에서는 한 번만 조회되었는지 확인
            verify(ticketRepository, times(1)).findById(savedTicket.getId());
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

    @Nested
    @DisplayName("cacheTicket 메소드는")
    class Describe_cacheTicket {

        @Test
        @DisplayName("티켓을 캐시에 저장한다")
        void saveTicketToCache() {

            // When
            ticketCacheService.cacheTicket(savedTicket);

            // Then
            Ticket cachedTicket = ticketCacheService.getTicket(savedTicket.getId());
            assertThat(cachedTicket).isEqualTo(savedTicket);
            verify(ticketRepository, never()).findById(savedTicket.getId());
        }
    }

    @Nested
    @DisplayName("invalidateTicketCache 메소드는")
    class Describe_invalidateTicketCache {

        @Test
        @DisplayName("캐시된 정보를 무효화한다")
        void invalidateTicketInfo() {
            // Given
            ticketCacheService.getTicket(savedTicket.getId());

            // When
            ticketCacheService.invalidateTicketCache(savedTicket.getId());

            // Then
            verify(ticketRepository, times(1)).findById(savedTicket.getId());
            Ticket invalidatedTicket = ticketCacheService.getTicket(savedTicket.getId());
            assertThat(invalidatedTicket).isNotNull();
            assertThat(invalidatedTicket.getId()).isEqualTo(savedTicket.getId());
            verify(ticketRepository, times(2)).findById(savedTicket.getId());
        }
    }

    @Nested
    @DisplayName("getCacheStats 메소드는")
    class Describe_getCacheStats {

        @Test
        @DisplayName("캐시 통계를 반환한다")
        void returnCacheStats() {
            // Given
            ticketCacheService.getTicket(savedTicket.getId());

            // When
            CacheStats stats = ticketCacheService.getCacheStats();

            // Then
            assertThat(stats).isNotNull();
            assertThat(stats.hitCount()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("clearCache 메소드는")
    class Describe_clearCache {

        @Test
        @DisplayName("모든 캐시를 삭제한다")
        void clearAllCache() {
            // Given
            ticketCacheService.getTicket(savedTicket.getId());

            // When
            ticketCacheService.clearCache();

            // Then
            verify(ticketRepository, times(1)).findById(savedTicket.getId());
            ticketCacheService.getTicket(savedTicket.getId());
            verify(ticketRepository, times(2)).findById(savedTicket.getId());
        }
    }
}