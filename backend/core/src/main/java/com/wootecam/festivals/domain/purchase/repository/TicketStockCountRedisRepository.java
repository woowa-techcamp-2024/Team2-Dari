package com.wootecam.festivals.domain.purchase.repository;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/*
    구매 가능한 티켓 재고 수량을 관리하는 Repository
    TicketStockCount: String 으로 구현되어 있으며 구매 가능한 티켓 수량을 관리
    - tickets:{ticketId}:ticketStocks:count:{}
 */
@Repository
@Slf4j
public class TicketStockCountRedisRepository extends RedisRepository {

    private static final String CHECK_AND_DECREASE_STOCK_SCRIPT = """
            local function executeScript()
                local stockKey = KEYS[1]
                local waitKey = KEYS[2]
                local recentRequestTimeKey = KEYS[3]
                local userId = ARGV[1]
                local ticketStockCount = tonumber(redis.call('GET', stockKey))

                if not ticketStockCount then
                    error("Failed to get ticket stock count")
                end

                if ticketStockCount > 0 then
                    -- Step 1: Decrease stock count
                    local decrementResult = redis.call('DECR', stockKey)
                    if not decrementResult or decrementResult < 0 then
                        error("Failed to decrement stock count")
                    end

                    -- Step 2: Remove from waiting list
                    local recentRequestTime = redis.call('GET', recentRequestTimeKey)
                    local delResult = redis.call('DEL', recentRequestTimeKey)
                    if delResult == 0 then
                        redis.call('INCR', stockKey)  -- Rollback the decrement
                        error("Failed to delete recent request time")
                    end

                    -- Step 3: Delete recent request time
                    local zremResult = redis.call('ZREM', waitKey, userId)
                    if zremResult == 0 then
                        redis.call('INCR', stockKey)  -- Rollback the decrement
                        redis.call('SET', recentRequestTimeKey, recentRequestTime)  -- Rollback the recentRequestTime
                        error("Failed to remove from waiting list")
                    end

                    return 1
                end

                return 0
            end

            local status, result = pcall(executeScript)
            if not status then
                return redis.error_reply("Lua script error: " .. result)
            end
            return result
            """;

    public TicketStockCountRedisRepository(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    /*
        티켓 재고 수량을 가져오는 메소드
        존재하지 않는 티켓이라면 null 반환
     */
    public Long getTicketStockCount(Long ticketId) {
        String value = redisTemplate.opsForValue().get(createKey(ticketId));
        return value == null ? null : Long.parseLong(value);
    }

    /*
        티켓 재고 수량을 설정하는 메소드
     */
    public void setTicketStockCount(Long ticketId, Long count) {
        redisTemplate.opsForValue()
                .set(createKey(ticketId), String.valueOf(count));
    }

    /*
        연산의 결과를 반환
        재고가 음수가 되는 경우에 대한 에러 핸들링 없음
     */
    public Long decreaseTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().increment(createKey(ticketId), -1);
    }

    /*
        lua script 를 이용한 재고 확인 및 재고 있는 경우 차감, 대기열 제거 메소드
     */
    public boolean checkAndDecreaseStock(Long ticketId, Long loginMemberId) {
        String stockKey = createKey(ticketId);
        String waitingKey = createWaitingKey(ticketId);
        String recentRequestTimeKey = createRecentRequestTimeKey(ticketId, loginMemberId);

        RedisScript<Long> script = RedisScript.of(CHECK_AND_DECREASE_STOCK_SCRIPT, Long.class);
        List<String> keys = Arrays.asList(stockKey, waitingKey, recentRequestTimeKey);
        String args = String.valueOf(loginMemberId);

        try {
            Long result = redisTemplate.execute(script, keys, args);
            return result != null && result == 1;
        } catch (InvalidDataAccessApiUsageException e) {
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            if (StringUtils.hasText(message) && message.contains("Lua script error:")) {
                log.warn("재고 확인 및 차감, 대기열 제거 로직이 실패했습니다. - {}", message);
                throw new IllegalStateException(message, e);
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /*
        연산의 결과를 반환
     */
    public Long increaseTicketStockCount(Long ticketId) {
        return redisTemplate.opsForValue().increment(createKey(ticketId), 1);
    }

    private String createKey(Long ticketId) {
        return TICKETS_PREFIX + ticketId + ":" + TICKET_STOCK_COUNT_PREFIX;
    }

    private String createWaitingKey(Long ticketId) {
        return TICKETS_PREFIX + ticketId + ":waitings";
    }

    private String createRecentRequestTimeKey(Long ticketId, Long userId) {
        return TICKETS_PREFIX + ticketId + ":users:" + userId;
    }
}
