package com.wootecam.festivals.domain.wait.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    대기열 순번 관리를 위한 Repository
    Waiting 은 SortedSet 으로 구현되며, value 는 userId, score 은 timestamp 로 구성
    대기열 사용자의 최근 요청 시각은 string으로 구현
 */
@Repository
@RequiredArgsConstructor
public class WaitingRepository {

    @Value("${redis.item.prefix}")
    private String ticketPrefix;

    private final RedisTemplate<String, String> redisTemplate;

    //TODO connectAsync 알아보기
    //TODO 코어 모듈 리팩토링 후 TimeProvider 로 바꾸기
    public Long addWaiting(Long ticketId, Long userId, Long ttlSeconds) {
        redisTemplate.opsForValue()
                .set(createRecentRequestTimeKey(ticketId, userId), String.valueOf(System.currentTimeMillis()),
                        ttlSeconds);
        redisTemplate.opsForZSet().add(createKey(ticketId), String.valueOf(userId), System.currentTimeMillis());
        return userId;
    }

    public void removeWaiting(Long ticketId, Long userId) {
        redisTemplate.opsForZSet().remove(createKey(ticketId), String.valueOf(userId));
    }

    public Long extendWaiting(Long ticketId, Long userId, Long ttlSeconds) {
        redisTemplate.opsForValue()
                .set(createRecentRequestTimeKey(ticketId, userId), String.valueOf(System.currentTimeMillis()),
                        ttlSeconds);
        return userId;
    }

    public Long getWaitingCount(Long ticketId, Long userId) {
        return redisTemplate.opsForZSet().rank(createKey(ticketId), String.valueOf(userId));

    }

    private String createKey(Long ticketId) {
        return ticketPrefix + ticketId + ":waitings";
    }

    private String createRecentRequestTimeKey(Long ticketId, Long userId) {
        return ticketPrefix + ticketId + ":users:" + userId;
    }
}
