package com.wootecam.festivals.domain.wait.repository;

import com.wootecam.festivals.domain.purchase.repository.RedisRepository;
import java.util.Set;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;

/*
    대기열 순번 관리를 위한 Repository
    Waiting 은 SortedSet 으로 구현되며, value 는 userId, score 은 timestamp 로 구성
    Waiting expire 은 스케줄링으로 앞의 n 개만 시간을 확인하여 삭제하도록 합니다.
 */
@Repository
public class WaitingRedisRepository extends RedisRepository {

    public WaitingRedisRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /*
        대기열에 사용자를 추가합니다.
        @return 추가에 성공했다면 true, 이미 대기열에 존재하는 사용자라면 false
     */
    public Long addWaiting(Long ticketId, Long userId) {
        long now = System.currentTimeMillis();

        redisTemplate.opsForValue().set(createRecentRequestTimeKey(ticketId, userId), String.valueOf(now));
        redisTemplate.opsForZSet().add(createKey(ticketId), String.valueOf(userId), now);

        return userId;
    }

    /*
        대기열에서 사용자를 제거합니다.
        redis 에서 제거된 원소의 개수를 반환
     */
    public void removeWaiting(Long ticketId, Long userId) {
        redisTemplate.delete(createRecentRequestTimeKey(ticketId, userId));
        redisTemplate.opsForZSet().remove(createKey(ticketId), String.valueOf(userId));
    }

    public void updateRecentRequestTime(Long ticketId, Long userId) {
        long now = System.currentTimeMillis();
        redisTemplate.opsForValue().set(createRecentRequestTimeKey(ticketId, userId), String.valueOf(now));
    }

    /*
        대기열 순번을 반환하는 메소드
     */
    public Long getWaitingCount(Long ticketId, Long userId) {
        return redisTemplate.opsForZSet().rank(createKey(ticketId), String.valueOf(userId));
    }

    public Long getRecentRequestTime(Long ticketId, Long userId) {
        String recentRequestTime = redisTemplate.opsForValue().get(createRecentRequestTimeKey(ticketId, userId));
        return recentRequestTime == null ? null : Long.parseLong(recentRequestTime);
    }

    /*
        대기열에서 앞의 n 개를 삭제합니다.
        제거된 원소 개수 반환
    */
    public Long removeFirstNWaitings(Long ticketId, Long n) {
        return redisTemplate.execute(new SessionCallback<>() {
            @Override
            public <K, V> Long execute(RedisOperations<K, V> operations) throws DataAccessException {
                String key = createKey(ticketId);

                operations.multi();

                Set<String> memberIds = (Set<String>) operations.opsForZSet().range((K) key, 0, n - 1);
                if (memberIds != null && !memberIds.isEmpty()) {
                    operations.opsForZSet().removeRange((K) key, 0, n - 1);
                    for (String memberId : memberIds) {
                        operations.delete((K) createRecentRequestTimeKey(ticketId, Long.parseLong(memberId)));
                    }
                }

                operations.exec();

                return (long) (memberIds != null ? memberIds.size() : 0);
            }
        });
    }

    public Long findByPosition(Long ticketId, Long position) {
        Set<String> element = redisTemplate.opsForZSet().range(createKey(ticketId), position, position);
        if (element != null && !element.isEmpty()) {
            return Long.valueOf(element.iterator().next());
        }
        return null;
    }

    private String createKey(Long ticketId) {
        return TICKETS_PREFIX + ticketId + ":waitings";
    }

    private String createRecentRequestTimeKey(Long ticketId, Long userId) {
        return TICKETS_PREFIX + ticketId + ":users:" + userId;
    }
}
