package com.wootecam.festivals.domain.purchase.repository;

import com.wootecam.festivals.domain.purchase.entity.TicketInfo;
import java.time.LocalDateTime;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 *   티켓 정보를 관리하는 Repository
 *
 *   Hash 로 구현되어 있으며 티켓 메타 데이터를 관리
 *   key : tickets:{ticketId}
 *   hashKey : startSaleTime, endSaleTime
 *
 *   tickets:{ticketId}:startSaleTime 티켓 판매 시작 시각
 *   tickets:{ticketId}:endSaleTime 티켓 판매 종료 시각
 */
@Repository
public class TicketInfoRedisRepository extends RedisRepository {

    public TicketInfoRedisRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /**
     *    티켓 정보를 가져오는 메소드
     *    존재하지 않는 티켓이라면 null 반환
     */
    public TicketInfo getTicketInfo(Long ticketId) {
        String startSaleTime = (String) redisTemplate.opsForHash().get(TICKETS_PREFIX + ticketId, TICKET_INFO_START_SALE_TIME_PREFIX);
        String endSaleTime = (String) redisTemplate.opsForHash().get(TICKETS_PREFIX + ticketId, TICKET_INFO_END_SALE_TIME_PREFIX);

        if (startSaleTime == null || endSaleTime == null) {
            return null;
        }

        return new TicketInfo(LocalDateTime.parse(startSaleTime), LocalDateTime.parse(endSaleTime));
    }

    /**
     * 티켓 정보를 설정하는 메소드
     */
    public void setTicketInfo(Long ticketId, LocalDateTime startSaleTime, LocalDateTime endSaleTime) {
        Assert.notNull(startSaleTime, "티켓 판매 시작 시각은 null 일 수 없습니다.");
        Assert.notNull(endSaleTime, "티켓 판매 종료 시각은 null 일 수 없습니다.");

        redisTemplate.opsForHash().put(TICKETS_PREFIX + ticketId, TICKET_INFO_START_SALE_TIME_PREFIX, startSaleTime.toString());
        redisTemplate.opsForHash().put(TICKETS_PREFIX + ticketId, TICKET_INFO_END_SALE_TIME_PREFIX, endSaleTime.toString());
    }
}
