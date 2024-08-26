package com.wootecam.festivals.domain.ticket.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.wootecam.festivals.domain.ticket.dto.CachedTicketInfo;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TicketCacheService {

    private static final int CACHE_MAX_SIZE = 1000;

    private final Cache<Long, CachedTicketInfo> ticketCache;
    private final TicketRepository ticketRepository;

    public TicketCacheService(TicketRepository ticketRepository) {
        this.ticketCache = Caffeine.newBuilder()
                .maximumSize(CACHE_MAX_SIZE) // 캐시의 최대 크기
                .expireAfterWrite(1, TimeUnit.HOURS) // 캐시에 쓰여진 후 1시간 뒤 만료
                .expireAfterAccess(30, TimeUnit.MINUTES) // 항목에 마지막으로 접근한 후 30분 뒤 만료
                .refreshAfterWrite(50, TimeUnit.MINUTES) // 항목이 쓰여진 후 50분이 지나면 비동기적으로 리프레시 -> 캐시 최신 상태 유지
                .recordStats() // 통계 기록
                .removalListener((key, value, cause) -> {
                    log.debug("Key " + key + " was removed (" + cause + ")");
                })
                .build(key -> fetchTicketInfo(key));
        this.ticketRepository = ticketRepository;
    }

    public CachedTicketInfo getTicketInfo(Long id) {
        return ticketCache.get(id, this::fetchTicketInfo);
    }

    public void cacheTicketInfo(CachedTicketInfo ticketInfo) {
        ticketCache.put(ticketInfo.id(), ticketInfo);
    }

    public void invalidateTicketInfo(Long ticketId) {
        ticketCache.invalidate(ticketId);
    }

    public CacheStats getCacheStats() {
        return ticketCache.stats();
    }

    public void clearCache() {
        this.ticketCache.invalidateAll();
    }

    private CachedTicketInfo fetchTicketInfo(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ApiException(TicketErrorCode.TICKET_NOT_FOUND));

        CachedTicketInfo cachedTicketInfo = CachedTicketInfo.from(ticket);

        return cachedTicketInfo;
    }
}
