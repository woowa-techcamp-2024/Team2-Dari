package com.wootecam.festivals.domain.purchase.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    구매 가능한 티켓 재고 리스트와 수량을 관리하는 Repository
    TicketStock : Set 으로 구현되어 있으며 구매 가능한 티켓 재고만을 관리 (결제 페이지에 입장했을 경우 remove 됨)
    - tickets:ticketId:ticketStocks:{}

    TicketStockCount: String 으로 구현되어 있으며 구매 가능한 티켓 수량을 관리
    - tickets:ticketId:ticketStocks:count:{}
 */
@Repository
public class TicketStockRedisRepository extends RedisRepository {

    public TicketStockRedisRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /*
        티켓 재고 수량을 가져오는 메소드
        존재하지 않는 티켓이라면 null 반환
     */
    public String getTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().get(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX);
    }

    /*
        티켓 재고 수량을 설정하는 메소드
     */
    public void setTicketStockCount(Long ticketId, Long count) {
        redisTemplate.opsForValue()
                .set(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX, String.valueOf(count));
    }

    /*
        연산의 결과를 반환
        재고가 음수가 되는 경우에 대한 에러 핸들링 없음
     */
    public Long decreaseTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().increment(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX, -1);
    }

    /*
        연산의 결과를 반환
     */
    public Long increaseTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().increment(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX, 1);
    }
}
