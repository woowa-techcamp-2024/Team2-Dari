package com.wootecam.festivals.domain.purchase.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public abstract class RedisRepository {

    public final String TICKETS_PREFIX = "tickets:";
    public final String WAITINGS_PREFIX = "waitings:";
    public final String TICKET_STOCK_COUNT_PREFIX = "ticketStocks:count:";
    public final String PURCHASED_MEMBERS_PREFIX = "purchasedMembers:";
    public final String CURRENTLY_PAYING_MEMBERS_PREFIX = "currentlyPayingMembers:";

    protected final RedisTemplate<String, String> redisTemplate;
}
