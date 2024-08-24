package com.wootecam;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    대기열 순번 관리를 위한 Repository
    Waiting 은 SortedSet 으로 구현되며, value 는 userId, score 은 timestamp 로 구성
    userId 는 TTL 를 위해 Strings 로 구성
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
        redisTemplate.opsForValue().set("userId:" + userId, String.valueOf(System.currentTimeMillis()), ttlSeconds);
        redisTemplate.opsForZSet().add(ticketPrefix + ticketId + "waitings", String.valueOf(userId), System.currentTimeMillis());
        return userId;
    }

    public void removeWaiting(Long ticketId, Long userId) {
        redisTemplate.opsForZSet().remove(ticketPrefix + ticketId + "waitings", String.valueOf(userId));
    }

    /*
        대기열 TTL 연장
        입장 순서는 아니지만 요청을 보낸 경우 ttl 를 refresh 하도록 함
     */
    public Long extendWaiting(Long ticketId, Long userId, Long ttlSeconds) {
        redisTemplate.opsForValue().set("userId:" + userId, String.valueOf(System.currentTimeMillis()), ttlSeconds);
        redisTemplate.opsForZSet().add(ticketPrefix + ticketId + "waitings", String.valueOf(userId), System.currentTimeMillis());
        return userId;
    }

    public Long getWaitingCount(Long ticketId, Long userId) {
        return redisTemplate.opsForZSet().rank(ticketPrefix + ticketId + "waitings", String.valueOf(userId));
    }

}
