package com.wootecam.festivals.repository;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/*
    구매 가능한 티켓 수량을 관리하는 Repository
    TicketStock : Set 으로 구현되어 있으며 구매 가능한 티켓 재고만을 관리 -> lock 를 걸지 않음에 대해 논의 필요
    - tickets:ticketId:ticketStocks:{}
    TicketStockCount: String 으로 구현되어 있으며 구매 가능한 티켓 수량을 관리
    - tickets:ticketId:ticketStocks:count
 */
@Repository
@RequiredArgsConstructor
public class TicketStockRepository {

    @Value("${redis.item.prefix}")
    private String ticketPrefix;

    private RedisTemplate<String, String> redisTemplate;

    /*
        티켓 재고 리스트을 초기화하는 메소드
        추후에 ticketStock 모듈이 생기면 구현 예정
     */
    public void bulkInsertTicketStock(Long ticketId) {}

    public Long getTicketStockCount(Long ticketId) {
        return Long.parseLong(
                Objects.requireNonNull(
                        redisTemplate.opsForValue().get(ticketPrefix + ticketId + ":" + "ticketStocks:count")));
    }

    public void setTicketStockCount(Long ticketId, Long count) {
        redisTemplate.opsForValue().set(ticketPrefix + ticketId + ":" + "ticketStocks:count", String.valueOf(count));
    }

    public Long decreaseTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().increment(ticketPrefix + ticketId + ":" + "ticketStocks:count", -1);
    }

    public Long increaseTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().increment(ticketPrefix + ticketId + ":" + "ticketStocks:count", 1);
    }

    /*
        남은 티켓 재고 중 하나를 가져오는 메소드
        해당 메소드와 decreaseTicketStockCount 을 atomic 하게 처리하기 위해 Redis 의 transaction 을 사용해야 함
        점유한 티켓재고 id 를 반환
     */
    public Long removeTicketStock(Long ticketId) {
        return Long.parseLong(
                Objects.requireNonNull(redisTemplate.opsForSet().pop(ticketPrefix + ticketId + ":" + "ticketStocks")));
    }

    public Long addTicketStock(Long ticketId, Long ticketStockId) {
        return redisTemplate.opsForSet().add(ticketPrefix + ticketId + ":" + "ticketStocks" + ":", String.valueOf(ticketStockId));
    }
}
