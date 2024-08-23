package com.wootecam;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    대기열 순번 관리를 위한 Repository
    Waiting 은 SortedSet 으로 구현되며, value 는 userId, score 은 timestamp 로 구성
    Waiting expire 은 스케줄링으로 앞의 n 개만 시간을 확인하여 삭제하도록 합니다.
 */
@Repository
public class WaitingRepository extends RedisRepository {

    public WaitingRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /*
    대기열에 사용자를 추가합니다.
    @return 추가에 성공했다면 true, 이미 대기열에 존재하는 사용자라면 false
     */
    public Boolean addWaiting(Long ticketId, Long userId) {
        return redisTemplate.opsForZSet().add(TICKETS_PREFIX + ticketId + ":" + WAITINGS_PREFIX, String.valueOf(userId),
                System.currentTimeMillis());
    }

    /*
    대기열에서 사용자를 제거합니다.
    redis 에서 제거된 원소의 개수를 반환
     */
    public Long removeWaiting(Long ticketId, Long userId) {
        return redisTemplate.opsForZSet().remove(TICKETS_PREFIX + ticketId + ":" + WAITINGS_PREFIX, String.valueOf(userId));
    }

    /*
    대기열 순번을 반환하는 메소드
     */
    public Long getWaitingCount(Long ticketId, Long userId) {
        return redisTemplate.opsForZSet()
                .rank(TICKETS_PREFIX + ticketId + ":" + WAITINGS_PREFIX, String.valueOf(userId));
    }

    /*
    대기열에서 앞의 n 개를 삭제합니다.
    제거된 원소 개수 반환
     */
    public Long removeFirstNWaitings(Long ticketId, Long n) {
        return redisTemplate.opsForZSet().removeRange(TICKETS_PREFIX + ticketId + ":" + WAITINGS_PREFIX, 0, n - 1);
    }
}
