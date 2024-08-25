package com.wootecam;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
abstract class RedisRepository {

    protected final String TICKETS_PREFIX = "tickets:";
    protected final String TICKET_STOCKS_PREFIX = "ticketStocks:";
    protected final String WAITINGS_PREFIX = "waitings:";
    protected final String TICKET_STOCK_COUNT_PREFIX = "ticketStocks:count:";
    protected final String PURCHASED_MEMBERS_PREFIX = "purchasedMembers:";
    protected final String CURRENTLY_PAYING_MEMBERS_PREFIX = "currentlyPayingMembers:";

    protected final RedisTemplate<String, String> redisTemplate;
}
