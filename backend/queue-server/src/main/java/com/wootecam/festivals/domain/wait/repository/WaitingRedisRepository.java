package com.wootecam.festivals.domain.wait.repository;

import com.wootecam.festivals.domain.purchase.repository.RedisRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    대기열 순번 관리를 위한 Repository
    Waiting 은 Set 으로 구현되며, 사용자 id를 value 저장합니다.
 */
@Repository
public class WaitingRedisRepository extends RedisRepository {

    public WaitingRedisRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /*
        대기열에 사용자를 추가합니다.
     */
    public void addWaiting(Long ticketId, Long userId) {
        redisTemplate.opsForSet().add(createKey(ticketId), String.valueOf(userId));
    }

    /*
        대기열 전체 사이즈를 반환하는 메소드
     */
    public Long getSize(Long ticketId) {
        return redisTemplate.opsForSet().size(createKey(ticketId));
    }

    /*
        대기열에 존재하는지 여부를 반환하는 메소드
     */
    public Boolean exists(Long ticketId, Long userId) {
        Boolean member = redisTemplate.opsForSet().isMember(createKey(ticketId), String.valueOf(userId));
        return redisTemplate.opsForSet().isMember(createKey(ticketId), String.valueOf(userId));
    }

    private String createKey(Long ticketId) {
        return TICKETS_PREFIX + ticketId + ":waitings";
    }
}
