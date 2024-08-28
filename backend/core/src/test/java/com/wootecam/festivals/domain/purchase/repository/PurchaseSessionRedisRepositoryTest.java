package com.wootecam.festivals.domain.purchase.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.wootecam.festivals.utils.TestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@DisplayName("PurchaseSessionRedisRepository 클래스")
class PurchaseSessionRedisRepositoryTest {

    private final Long ticketId = 1L;
    private final Long memberId = 1L;
    private final String sessionId = "session123";
    private final Long ticketStockId = 100L;
    private final Long ttl = 5L; // TTL 5분
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PurchaseSessionRedisRepository purchaseSessionRedisRepository;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll(); // Redis 초기화
    }

    @Test
    @DisplayName("addPurchaseSession 메소드는 구매 세션을 추가한다")
    void it_adds_purchase_session() {
        // When
        purchaseSessionRedisRepository.addPurchaseSession(ticketId, memberId, sessionId, ticketStockId, ttl);

        // Then
        String key = "tickets:" + ticketId + ":purchaseSessions:" + sessionId + ":members:" + memberId;
        assertThat(redisTemplate.hasKey(key)).isTrue();
    }

    @Test
    @DisplayName("exists 메소드는 구매 세션이 존재하는지 확인한다")
    void it_checks_if_purchase_session_exists() {
        // Given
        purchaseSessionRedisRepository.addPurchaseSession(ticketId, memberId, sessionId, ticketStockId, ttl);

        // When
        Boolean exists = purchaseSessionRedisRepository.exists(ticketId, sessionId, memberId);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("getPurchaseSessionValue 메소드는 구매 세션 값을 반환한다")
    void it_returns_purchase_session_value() {
        // Given
        purchaseSessionRedisRepository.addPurchaseSession(ticketId, memberId, sessionId, ticketStockId, ttl);

        // When
        String value = purchaseSessionRedisRepository.getPurchaseSessionValue(ticketId, sessionId, memberId);

        // Then
        assertThat(value).isEqualTo(String.valueOf(ticketStockId));
    }

    @Test
    @DisplayName("removePurchaseSession 메소드는 구매 세션을 삭제한다")
    void it_removes_purchase_session() {
        // Given
        purchaseSessionRedisRepository.addPurchaseSession(ticketId, memberId, sessionId, ticketStockId, ttl);

        // When
        purchaseSessionRedisRepository.removePurchaseSession(ticketId, sessionId, memberId);

        // Then
        Boolean exists = purchaseSessionRedisRepository.exists(ticketId, sessionId, memberId);
        assertThat(exists).isFalse();
    }
}
