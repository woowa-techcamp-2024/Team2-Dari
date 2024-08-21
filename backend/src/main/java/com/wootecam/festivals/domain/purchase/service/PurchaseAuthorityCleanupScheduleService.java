package com.wootecam.festivals.domain.purchase.service;

import static com.wootecam.festivals.domain.purchase.controller.PurchaseController.PURCHASABLE_TICKET_KEY;
import static com.wootecam.festivals.domain.purchase.controller.PurchaseController.PURCHASABLE_TICKET_TIMESTAMP_KEY;

import com.wootecam.festivals.global.config.CustomMapSessionRepository;
import com.wootecam.festivals.global.utils.TimeProvider;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseAuthorityCleanupScheduleService {

    private final TimeProvider timeProvider;
    private final CustomMapSessionRepository sessionRepository;
    private final TicketStockRollbacker ticketStockRollbackService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void cleanUpSessions() {
        LocalDateTime now = timeProvider.getCurrentTime();
        log.debug("티켓 구매 권한 삭제 - 현재 시각 {}", now);

        Collection<Session> sessions = sessionRepository.getSessions();
        Map<Long, Integer> rollbackTicketStocks = new HashMap<>();
        for (Session session : sessions) {
            Object purchasableTicketTimestampObj = session.getAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY);
            Object purchasableTicketIdObj = session.getAttribute(PURCHASABLE_TICKET_KEY);
            if (purchasableTicketTimestampObj == null || purchasableTicketIdObj == null) {
                continue;
            }

            LocalDateTime purchasableTicketTimestamp = LocalDateTime.parse(
                    String.valueOf(purchasableTicketTimestampObj));
            Long purchasableTicketId = (Long) purchasableTicketIdObj;

            if (purchasableTicketTimestamp.isBefore(now)) {
                session.removeAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY);
                session.removeAttribute(PURCHASABLE_TICKET_KEY);

                log.debug("티켓 구매 권한 삭제 - sid {}, timestamp {}, ticketId {}",
                        session.getId(), purchasableTicketTimestamp, purchasableTicketId);
                rollbackTicketStocks.put(purchasableTicketId, rollbackTicketStocks.getOrDefault(purchasableTicketId, 0) + 1);
            }
        }

        rollbackTicketStocks.forEach(ticketStockRollbackService::rollbackTicketStock);
    }
}
