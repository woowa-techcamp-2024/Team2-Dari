package com.wootecam.festivals.domain.purchase.service;

import static com.wootecam.festivals.domain.purchase.controller.PurchaseController.PURCHASABLE_TICKET_STOCK_KEY;
import static com.wootecam.festivals.domain.purchase.controller.PurchaseController.PURCHASABLE_TICKET_TIMESTAMP_KEY;

import com.wootecam.festivals.global.config.CustomMapSessionRepository;
import com.wootecam.festivals.global.utils.TimeProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
        List<Long> rollbackTicketStocks = new ArrayList<>();
        for (Session session : sessions) {
            Object purchasableTicketTimestampObj = session.getAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY);
            Object purchasableTicketStockIdObj = session.getAttribute(PURCHASABLE_TICKET_STOCK_KEY);
            if (purchasableTicketTimestampObj == null || purchasableTicketStockIdObj == null) {
                continue;
            }

            LocalDateTime purchasableTicketTimestamp = LocalDateTime.parse(
                    String.valueOf(purchasableTicketTimestampObj));
            Long purchasableTicketStockId = (Long) purchasableTicketStockIdObj;

            if (purchasableTicketTimestamp.isBefore(now)) {
                session.removeAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY);
                session.removeAttribute(PURCHASABLE_TICKET_STOCK_KEY);

                log.debug("티켓 구매 권한 삭제 - sid {}, timestamp {},  {}",
                        session.getId(), purchasableTicketTimestamp, purchasableTicketStockId);
                rollbackTicketStocks.add(purchasableTicketStockId);
            }
        }
        rollbackTicketStocks.forEach(ticketStockRollbackService::rollbackTicketStock);
    }
}
