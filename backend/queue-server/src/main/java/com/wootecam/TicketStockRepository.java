package com.wootecam;

import java.util.Objects;
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
public class TicketStockRepository extends RedisRepository {

    public TicketStockRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /*
            티켓 재고 리스트을 초기화하는 메소드
            추후에 ticketStock 모듈이 생기면 구현 예정
         */
    public void bulkInsertTicketStock(Long ticketId) {
    }

    /*
        티켓 재고 수량을 가져오는 메소드
        존재하지 않는 티켓이라면 NPE 발생
     */
    public Long getTicketStockCount(Long ticketId) {
        return Long.parseLong(redisTemplate.opsForValue().get(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX));
    }

    /*
        티켓 재고 수량을 설정하는 메소드
     */
    public void setTicketStockCount(Long ticketId, Long count) {
        redisTemplate.opsForValue()
                .set(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX, String.valueOf(count));
    }

    /*
        재고가 음수가 되는 경우에 대한 에러 핸들링 없음
     */
    public Long decreaseTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().increment(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX, -1);
    }

    public Long increaseTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().increment(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX, 1);
    }

    /*
        남은 티켓 재고 중 하나를 가져오는 메소드
        해당 메소드와 decreaseTicketStockCount 을 atomic 하게 처리하기 위해 Redis 의 transaction 을 사용해야 함
        점유한 티켓재고 id 를 반환
     */
    public Long removeTicketStock(Long ticketId) {
        return Long.parseLong(Objects.requireNonNull(
                redisTemplate.opsForSet().pop(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCKS_PREFIX)));
    }

    /*
        티켓 재고 리스트에 티켓 재고를 추가하는 메소드
        1 : 추가 성공, 0 : 이미 존재하는 재고라서 업데이트
     */
    public Long addTicketStock(Long ticketId, Long ticketStockId) {
        return redisTemplate.opsForSet()
                .add(TICKETS_PREFIX + ticketId + ":" + TICKET_STOCKS_PREFIX, String.valueOf(ticketStockId));
    }
}
