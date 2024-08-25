package com.wootecam.festivals.domain.purchase.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    티켓 구매 권한을 관리하는 Repository
    자료 구조는 String을 사용
    - key: tickets:{ticketId}:purchaseSessions:{purchaseSessionId}:
    - value: members:{memberId}:ticketStocks:{ticketStockId}
 */
@Repository
@RequiredArgsConstructor
public class PurchaseSessionRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void addPurchaseSession(Long ticketId, Long memberId, Long ticketStockId, String sessionId, Long ttl) {
        String key = createPurchaseSessionKey(ticketId, sessionId);
        String value = createPurchaseSessionValue(memberId, ticketStockId);
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.MINUTES);
    }

    public void removePurchaseSession(Long ticketId, String sessionId) {
        String key = createPurchaseSessionKey(ticketId, sessionId);
        redisTemplate.delete(key);
    }

    public Boolean exists(Long ticketId, String sessionId) {
        String key = createPurchaseSessionKey(ticketId, sessionId);
        return redisTemplate.hasKey(key);
    }

    public String getPurchaseSessionValue(Long ticketId, String sessionId) {
        String key = createPurchaseSessionKey(ticketId, sessionId);
        return redisTemplate.opsForValue().get(key);
    }

    private String createPurchaseSessionKey(Long ticketId, String sessionId) {
        return "tickets:" + ticketId + ":purchaseSessions:" + sessionId;
    }

    private String createPurchaseSessionValue(Long memberId, Long ticketStockId) {
        return "members:" + memberId + ":ticketStocks:" + ticketStockId;
    }
}
