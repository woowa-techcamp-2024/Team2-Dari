package com.wootecam;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import redis.embedded.RedisServer;

@SpringBootTest
@DisplayName("WaitingRepository 테스트")
class WaitingRepositoryTest {

    private static RedisServer redisServer;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private WaitingRepository waitingRepository;

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Nested
    @DisplayName("addWaiting 메소드는")
    class Describe_addWaiting {

        @Test
        @DisplayName("대기열에 사용자를 추가하고 rank 를 반환한다")
        void it_adds_user_to_waiting_queue_and_returns_rank() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;

            // When
            waitingRepository.addWaiting(ticketId, userId);

            // Then
            Long rank = waitingRepository.getWaitingCount(ticketId, userId);
            assertThat(rank).isZero();
            String key = waitingRepository.TICKETS_PREFIX + ticketId + ":" + waitingRepository.WAITINGS_PREFIX;
            assertThat(redisTemplate.opsForZSet().score(key, String.valueOf(userId))).isNotNull();
        }

        @Test
        @DisplayName("이미 존재하는 사용자의 경우 score를 업데이트하고 false를 반환한다")
        void it_updates_score_for_existing_user_and_returns_false() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;
            String key = waitingRepository.TICKETS_PREFIX + ticketId + ":" + waitingRepository.WAITINGS_PREFIX;

            waitingRepository.addWaiting(ticketId, userId);
            Double initialScore = redisTemplate.opsForZSet().score(key, String.valueOf(userId));

            // When
            boolean result = waitingRepository.addWaiting(ticketId, userId);

            // Then
            assertThat(result).isFalse();
            Double updatedScore = redisTemplate.opsForZSet().score(key, String.valueOf(userId));
            assertThat(updatedScore).isGreaterThan(initialScore);
        }


        @Test
        @DisplayName("대기열에 여러명의 사용자를 추가한 후 대기열에 사용자를 추가하면 rank 를 반환한다")
        void it_returns_rank_after_adding_multiple_users_to_waiting_queue() {
            // Given
            Long ticketId = 1L;
            Long userId1 = 100L;
            Long userId2 = 101L;
            Long userId3 = 102L;
            waitingRepository.addWaiting(ticketId, userId1);
            waitingRepository.addWaiting(ticketId, userId2);

            // When
            waitingRepository.addWaiting(ticketId, userId3);

            // Then
            Long rank = waitingRepository.getWaitingCount(ticketId, userId3);
            assertThat(rank).isEqualTo(2L); // 0-based index, so third user has rank 2
        }
    }

    @Nested
    @DisplayName("removeWaiting 메소드는")
    class Describe_removeWaiting {

        @Test
        @DisplayName("대기열에서 사용자를 제거한다")
        void it_removes_user_from_waiting_queue() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;
            waitingRepository.addWaiting(ticketId, userId);

            // When
            waitingRepository.removeWaiting(ticketId, userId);

            // Then
            String key = waitingRepository.TICKETS_PREFIX + ticketId + ":" + waitingRepository.WAITINGS_PREFIX;
            assertThat(redisTemplate.opsForZSet().score(key, String.valueOf(userId))).isNull();
        }
    }

    @Nested
    @DisplayName("getWaitingCount 메소드는")
    class Describe_getWaitingCount {

        @Test
        @DisplayName("대기열에서 사용자의 순위를 반환한다")
        void it_returns_user_rank_in_waiting_queue() {
            // Given
            Long ticketId = 1L;
            Long userId1 = 100L;
            Long userId2 = 101L;
            Long userId3 = 102L;
            waitingRepository.addWaiting(ticketId, userId1);
            waitingRepository.addWaiting(ticketId, userId2);
            waitingRepository.addWaiting(ticketId, userId3);

            // When
            Long result = waitingRepository.getWaitingCount(ticketId, userId2);

            // Then
            assertThat(result).isEqualTo(1L); // 0-based index, so second user has rank 1
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 경우 null을 반환한다")
        void it_returns_null_for_non_existent_user() {
            // Given
            Long ticketId = 1L;
            Long nonExistentUserId = 999L;

            // When
            Long result = waitingRepository.getWaitingCount(ticketId, nonExistentUserId);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("removeFirstNWaitings 메소드는")
    class Describe_removeFirstNWaitings {

        @Test
        @DisplayName("대기열에서 처음 N명의 사용자를 제거한다")
        void it_removes_first_n_users_from_waiting_queue() {
            // Given
            Long ticketId = 1L;
            for (long i = 1; i <= 5; i++) {
                waitingRepository.addWaiting(ticketId, i);
            }

            // When
            waitingRepository.removeFirstNWaitings(ticketId, 3L);

            // Then
            String key = waitingRepository.TICKETS_PREFIX + ticketId + ":" + waitingRepository.WAITINGS_PREFIX;
            Set<String> remainingUsers = redisTemplate.opsForZSet().range(key, 0, -1);
            assertThat(remainingUsers).containsExactly("4", "5");
        }

        @Test
        @DisplayName("N이 대기열의 크기보다 큰 경우 모든 사용자를 제거한다")
        void it_removes_all_users_when_n_is_greater_than_queue_size() {
            // Given
            Long ticketId = 1L;
            for (long i = 1; i <= 3; i++) {
                waitingRepository.addWaiting(ticketId, i);
            }

            // When
            waitingRepository.removeFirstNWaitings(ticketId, 5L);

            // Then
            String key = waitingRepository.TICKETS_PREFIX + ticketId + ":" + waitingRepository.WAITINGS_PREFIX;
            Set<String> remainingUsers = redisTemplate.opsForZSet().range(key, 0, -1);
            assertThat(remainingUsers).isEmpty();
        }
    }
}