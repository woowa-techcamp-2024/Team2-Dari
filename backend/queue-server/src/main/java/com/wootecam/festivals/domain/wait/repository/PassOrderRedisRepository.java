package com.wootecam.festivals.domain.wait.repository;

import com.wootecam.festivals.domain.ticket.repository.RedisRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    통과 대기열 범위 관리를 위한 Repository
    통과 대기열 은 String 으로 구성됩니다.
    - key: ticketId:{ticketId}
    - value: passOrder
 */
@Repository
public class PassOrderRedisRepository extends RedisRepository {

    public PassOrderRedisRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /*
        통과 대기열 범위를 증가시킵니다.
        - 증가된 값이 현재 대기열 순번보다 작거나 같을 경우 증가된 값을 반환합니다.
        - 증가된 값이 현재 대기열 순번보다 클 경우 현재 대기열 순번을 반환합니다.
     */
    public Long increase(Long ticketId, Long passOrderChunkSize, Long curWaitOrder) {
        String curPassOrderStr = redisTemplate.opsForValue().get(createKey(ticketId));
        Long curPassOrder = curPassOrderStr == null ? 0 : Long.parseLong(curPassOrderStr);
        Long newPassOrder = curPassOrder + passOrderChunkSize;
        if (newPassOrder <= curWaitOrder) {
            redisTemplate.opsForValue().set(createKey(ticketId), String.valueOf(newPassOrder));
            return newPassOrder;
        }
        return curPassOrder;
    }

    private String createKey(Long ticketId) {
        return TICKETS_PREFIX + ticketId + ":passOrder";
    }
}
