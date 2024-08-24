package com.wootecam.festivals.domain.purchase.service;

import com.wootecam.festivals.global.auth.purchase.PurchaseSession;
import com.wootecam.festivals.global.utils.TimeProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseAuthorityCleanupScheduleService {

    private final TimeProvider timeProvider;
    private final TicketStockRollbacker ticketStockRollbackService;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void cleanUpSessions() {
        try {
            LocalDateTime currentTime = timeProvider.getCurrentTime();
            log.debug("티켓 구매 권한 삭제 - 현재 시각 {}", currentTime);

            Set<String> keys = getPurchaseSessionKeys();
            List<String> sessions = redisTemplate.opsForValue().multiGet(keys);
            if (sessions == null) {
                log.warn("티켓 구매 권한 삭제 스케줄링 실행하였으나 티켓 구매 세션을 가져오지 못해 실행되지 않았습니다.");
                return;
            }

            List<String> keysToDelete = new ArrayList<>();
            Map<Long, Integer> rollbackTicketStocks = new HashMap<>();
            setSessionToDelete(sessions, keys, currentTime, keysToDelete, rollbackTicketStocks);

            if (keysToDelete.isEmpty()) {
                log.info("기간이 만료된 티켓 구매 권한이 없습니다.");
                return;
            }

            redisTemplate.delete(keysToDelete);
            log.debug("티켓 구매 권한 삭제 - 삭제된 세션 수: {}", keysToDelete.size());

            rollbackTicketStocks.forEach(ticketStockRollbackService::rollbackTicketStock);
            log.debug("티켓 재고 롤백 - 롤백된 티켓 정보: {}", rollbackTicketStocks);
        } catch (Exception e) {
            log.error("티켓 구매 권한 삭제 스케줄링 실행 중 오류가 발생하였습니다.", e);
        }
    }

    private Set<String> getPurchaseSessionKeys() {
        Set<String> keys = new HashSet<>();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(
                ScanOptions.scanOptions().match("purchase_session:*").count(100).build());

        while (cursor.hasNext()) {
            keys.add(new String(cursor.next()));
        }
        return keys;
    }

    private void setSessionToDelete(List<String> sessions, Set<String> keys, LocalDateTime currentTime,
                                    List<String> keysToDelete, Map<Long, Integer> rollbackTicketStocks) {
        int idx = 0;
        Object[] keysArray = keys.toArray();
        for (String sessionStr : sessions) {
            String key = (String) keysArray[idx];
            PurchaseSession session = PurchaseSession.of(sessionStr);

            if (session.isExpired(currentTime)) {
                log.debug("티켓 구매 권한 삭제 예정 - sid {}, timestamp {}, ticketId {}, memberId {}",
                        key, session.expirationTime(), session.ticketId(), session.memberId());
                keysToDelete.add(key);
                rollbackTicketStocks.put(session.ticketId(),
                        rollbackTicketStocks.getOrDefault(session.ticketId(), 0) + 1);
            }
            ++idx;
        }
    }
}