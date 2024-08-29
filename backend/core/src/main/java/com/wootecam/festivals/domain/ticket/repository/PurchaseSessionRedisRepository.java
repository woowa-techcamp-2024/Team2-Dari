package com.wootecam.festivals.domain.ticket.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    티켓 구매 권한을 관리하는 Repository
    자료 구조는 String을 사용
    - key: tickets:{ticketId}:purchaseSessions:{purchaseSessionId}:members:{memberId}
    - value: ticketStockId
 */
@Repository
@RequiredArgsConstructor
public class PurchaseSessionRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void addPurchaseSession(Long ticketId, Long memberId, String sessionId, Long ticketStockId, Long ttl) {
        String key = createPurchaseSessionKey(ticketId, sessionId, memberId);
        String value = createPurchaseSessionValue(ticketStockId);
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.MINUTES);
    }

    public void removePurchaseSession(Long ticketId, String sessionId, Long memberId) {
        String key = createPurchaseSessionKey(ticketId, sessionId, memberId);
        redisTemplate.delete(key);
    }

    public Boolean exists(Long ticketId, String sessionId, Long memberId) {
        String key = createPurchaseSessionKey(ticketId, sessionId, memberId);
        return redisTemplate.hasKey(key);
    }

    public String getPurchaseSessionValue(Long ticketId, String sessionId, Long memberId) {
        String key = createPurchaseSessionKey(ticketId, sessionId, memberId);
        return redisTemplate.opsForValue().get(key);
    }

    private String createPurchaseSessionKey(Long ticketId, String sessionId, Long memberId) {
        return "tickets:" + ticketId + ":purchaseSessions:" + sessionId + ":members:" + memberId;
    }

    private String createPurchaseSessionValue(Long ticketStockId) {
        return String.valueOf(ticketStockId);
    }
}
