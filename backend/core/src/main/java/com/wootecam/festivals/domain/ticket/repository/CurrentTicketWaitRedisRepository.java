package com.wootecam.festivals.domain.ticket.repository;

import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


/**
 * 현재 진행 중인 티켓팅의 티켓 ID를 관리합니다. 자료 구조는 Set을 사용합니다. - key: currentTicketWait
 */
@Repository
public class CurrentTicketWaitRedisRepository extends RedisRepository {

    private static final String KEY = "currentTicketWait";

    public CurrentTicketWaitRedisRepository(
            RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    public void addCurrentTicketWait(Long ticketId) {
        redisTemplate.opsForSet().add(KEY, String.valueOf(ticketId));
    }

    public void removeCurrentTicketWait(Long ticketId) {
        redisTemplate.opsForSet().remove(KEY, String.valueOf(ticketId));
    }

    public List<Long> getCurrentTicketWait() {
        return redisTemplate.opsForSet().members(KEY).stream()
                .map(Long::parseLong)
                .toList();
    }
}
