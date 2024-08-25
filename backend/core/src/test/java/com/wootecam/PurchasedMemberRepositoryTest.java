package com.wootecam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
@DisplayName("PurchasedMemberRepository 테스트")
class PurchasedMemberRepositoryTest {

    @Autowired
    private PurchasedMemberRepository purchasedMemberRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Nested
    @DisplayName("addPurchasedMember 메소드는")
    class Describe_addPurchasedMember {

        @Test
        @DisplayName("새로운 구매 회원을 추가하고 1을 반환한다")
        void it_adds_new_purchased_member_and_returns_one() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;

            // When
            Long result = purchasedMemberRepository.addPurchasedMember(ticketId, userId);

            // Then
            assertThat(result).isEqualTo(1L);
            assertThat(redisTemplate.opsForSet()
                    .isMember("tickets:" + ticketId + ":purchasedMembers", String.valueOf(userId))).isTrue();
        }

        @Test
        @DisplayName("이미 존재하는 구매 회원을 추가하면 0을 반환한다")
        void it_returns_zero_when_adding_existing_purchased_member() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;
            purchasedMemberRepository.addPurchasedMember(ticketId, userId);

            // When
            Long result = purchasedMemberRepository.addPurchasedMember(ticketId, userId);

            // Then
            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("removePurchasedMember 메소드는")
    class Describe_removePurchasedMember {

        @Test
        @DisplayName("구매 회원을 제거하고 지워진 원소의 개수인 1를 반환한다")
        void it_removes_purchased_member() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;
            purchasedMemberRepository.addPurchasedMember(ticketId, userId);

            // When
            Long result = purchasedMemberRepository.removePurchasedMember(ticketId, userId);

            // Then
            assertThat(result).isEqualTo(1L);
            assertThat(redisTemplate.opsForSet()
                    .isMember("tickets:" + ticketId + ":purchasedMembers", String.valueOf(userId))).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 구매 회원을 제거하면 0 를 반환하다")
        void it_does_not_throw_exception_when_removing_non_existent_member() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;

            // When & Then
            assertThat(purchasedMemberRepository.removePurchasedMember(ticketId, userId)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("isPurchasedMember 메소드는")
    class Describe_isPurchasedMember {

        @Test
        @DisplayName("구매한 회원인 경우 true를 반환한다")
        void it_returns_true_for_purchased_member() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;
            purchasedMemberRepository.addPurchasedMember(ticketId, userId);

            // When
            Boolean result = purchasedMemberRepository.isPurchasedMember(ticketId, userId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("구매하지 않은 회원인 경우 false를 반환한다")
        void it_returns_false_for_non_purchased_member() {
            // Given
            Long ticketId = 1L;
            Long userId = 100L;

            // When
            Boolean result = purchasedMemberRepository.isPurchasedMember(ticketId, userId);

            // Then
            assertThat(result).isFalse();
        }
    }
}