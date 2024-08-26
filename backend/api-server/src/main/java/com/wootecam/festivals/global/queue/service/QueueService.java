package com.wootecam.festivals.global.queue.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.wootecam.festivals.domain.checkin.entity.Checkin;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.queue.CustomQueue;
import com.wootecam.festivals.global.queue.InMemoryQueue;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.exception.QueueFullException;
import com.wootecam.festivals.global.utils.TimeProvider;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class QueueService {

    // 큐 사이즈 지정 안할시 1000이 기본
    private static final int QUEUE_SIZE = 3000;
    // 최대 재시도 횟수
    private static final int MAX_RETRY_COUNT = 3;
    // 배치 사이즈
    private static final int MAX_BATCH_SIZE = 2000;
    private static final int MIN_BATCH_SIZE = 100;
    // 캐시 사이즈
    private static final int TICKET_CACHE_SIZE = 100;

    // 주 큐: 처리할 구매 데이터를 저장
    private final CustomQueue<PurchaseData> queue = new InMemoryQueue<>(QUEUE_SIZE);
    // 에러 큐: 처리 실패한 구매 데이터를 저장
    private final ConcurrentLinkedQueue<PurchaseData> errorQueue = new ConcurrentLinkedQueue<>();
    // 재시도 횟수를 추적하는 맵
    private final ConcurrentMap<PurchaseData, Integer> retryCount = new ConcurrentHashMap<>();

    private final PurchaseRepository purchaseRepository;
    private final TicketRepository ticketRepository;
    private final MemberRepository memberRepository;
    private final TimeProvider timeProvider;
    private final JdbcTemplate jdbcTemplate;

    // 가용 프로세서 수에 맞춘 고정 크기 스레드 풀 생성
    private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final Cache<Long, Ticket> ticketCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(TICKET_CACHE_SIZE)
            .build();

    // 구매 데이터를 큐에 추가하는 메서드
    public void addPurchase(PurchaseData purchaseData) {
        if (purchaseData == null) {
            throw new IllegalArgumentException("Purchase data cannot be null");
        }
        try {
            queue.offer(purchaseData);
            log.debug("Added purchaseData to InMemoryQueue : {}", purchaseData);
        } catch (QueueFullException e) {
            log.error("Failed to add purchase to queue: {}", purchaseData, e);
            errorQueue.offer(purchaseData);
            log.warn("Purchase data added to error queue: {}", purchaseData);
        }
    }

    // 주기적으로 큐의 구매 데이터를 처리하는 메서드
    @Scheduled(fixedRate = 3000) // 3초마다 실행
    public void processPurchases() {
        int batchSize = calculateOptimalBatchSize();
        List<PurchaseData> batch = queue.pollBatch(batchSize);
        if (!batch.isEmpty()) {
            CompletableFuture.runAsync(() -> processBatch(batch), executor)
                    .exceptionally(e -> {
                        log.error("Fail to process purchase batch", e);
                        handleFailedBatch(batch);
                        return null;
                    });
        }
    }

    // 배치로 구매 데이터를 처리하는 메서드
    @Transactional
    protected void processBatch(List<PurchaseData> purchases) {
        List<Purchase> successfulPurchases = new ArrayList<>();
        for (PurchaseData purchase : purchases) {
            try {
                Purchase newPurchase = createPurchase(purchase);
                successfulPurchases.add(newPurchase);
            } catch (Exception e) {
                log.error("Failed to process purchase: {}", purchase, e);
            }
        }

        if (!successfulPurchases.isEmpty()) {
            batchInsertPurchases(successfulPurchases);
            batchInsertCheckins(successfulPurchases);
            synchronizeTicketStock();
        }
    }

    // 구매 엔티티를 생성하는 메서드
    private Purchase createPurchase(PurchaseData purchaseData) {
        Ticket ticket = ticketCache.get(purchaseData.ticketId(), this::getTicketFromDatabase);
        Member member = getMemberFromDatabase(purchaseData.memberId());
        return Purchase.builder()
                .ticket(ticket)
                .member(member)
                .purchaseTime(timeProvider.getCurrentTime())
                .purchaseStatus(PurchaseStatus.PURCHASED)
                .build();
    }

    private Ticket getTicketFromDatabase(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    private Member getMemberFromDatabase(Long memberId) {
        return memberRepository.getReferenceById(memberId);
    }

    // 구매 정보 벌크 인서트
    private void batchInsertPurchases(List<Purchase> purchases) {

        Timestamp now = new Timestamp(System.currentTimeMillis());

        jdbcTemplate.batchUpdate(
                "INSERT INTO purchase (ticket_id, member_id, purchase_time, purchase_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Purchase purchase = purchases.get(i);
                        ps.setLong(1, purchase.getTicket().getId());
                        ps.setLong(2, purchase.getMember().getId());
                        ps.setTimestamp(3, Timestamp.valueOf(purchase.getPurchaseTime()));
                        ps.setString(4, purchase.getPurchaseStatus().name());
                        ps.setTimestamp(5, now);
                        ps.setTimestamp(6, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return purchases.size();
                    }
                }
        );
        log.debug("Batch inserted {} purchases", purchases.size());
    }

    // 체크인 정보 벌크 인서트
    private void batchInsertCheckins(List<Purchase> purchases) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        List<Checkin> checkins = purchases.stream()
                .map(this::createCheckin)
                .toList();

        jdbcTemplate.batchUpdate(
                "INSERT INTO checkin (member_id, ticket_id, festival_id, checkin_time, is_checked, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Checkin checkin = checkins.get(i);
                        ps.setLong(1, checkin.getMember().getId());
                        ps.setLong(2, checkin.getTicket().getId());
                        ps.setLong(3, checkin.getFestival().getId());
                        ps.setTimestamp(4, null);  // 초기에는 체크인 시간이 없음
                        ps.setBoolean(5, false);   // 초기에는 체크인되지 않은 상태
                        ps.setTimestamp(6, now);
                        ps.setTimestamp(7, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return checkins.size();
                    }
                }
        );
        log.debug("Batch inserted {} checkins", checkins.size());
    }

    private Checkin createCheckin(Purchase purchase) {
        return Checkin.builder()
                .member(purchase.getMember())
                .ticket(purchase.getTicket())
                .build();
    }

    // 큐의 크기에 따라 최적의 배치 크기를 계산하는 메서드
    private int calculateOptimalBatchSize() {
        int queueSize = queue.size();
        return Math.max(MIN_BATCH_SIZE, Math.min(queueSize, MAX_BATCH_SIZE));
    }

    // 실패한 배치를 처리하는 메서드
    private void handleFailedBatch(List<PurchaseData> failedBatch) {
        errorQueue.addAll(failedBatch);
        log.warn("{} purchase data items added to error queue", failedBatch.size());
    }

    // 주기적으로 에러 큐의 항목들을 처리하는 메서드
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @Transactional
    public void processErrorQueue() {
        List<PurchaseData> errorBatch = new ArrayList<>();
        PurchaseData errorData;
        while ((errorData = errorQueue.poll()) != null && errorBatch.size() < 100) {
            errorBatch.add(errorData);
        }

        if (!errorBatch.isEmpty()) {
            log.info("Processing {} items from error queue", errorBatch.size());
            for (PurchaseData data : errorBatch) {
                try {
                    processSinglePurchase(data);
                    log.info("Successfully processed error item: {}", data);
                } catch (Exception e) {
                    handleRetry(data);
                }
            }
        }
    }

    // 단일 구매 데이터를 처리하는 메서드
    private void processSinglePurchase(PurchaseData data) {
        Purchase purchase = createPurchase(data);
        purchaseRepository.save(purchase);
    }

    // 재시도 로직을 처리하는 메서드
    private void handleRetry(PurchaseData data) {
        int count = retryCount.compute(data, (k, v) -> v == null ? 1 : v + 1);
        if (count <= MAX_RETRY_COUNT) {
            log.warn("Retry attempt {} for purchase data: {}", count, data);
            errorQueue.offer(data);
        } else {
            log.error("Max retry attempts reached for purchase data: {}", data);
            saveToPermanantErrorStorage(data);
            notifyAdministrator(data);
            retryCount.remove(data);
        }
    }

    // TODO: 영구 에러 저장소에 저장하는 메서드 구현 필요
    private void saveToPermanantErrorStorage(PurchaseData data) {
        // 영구 저장소(예: 데이터베이스의 별도 테이블)에 저장하는 로직
        log.error("Saving failed purchase to permanent error storage: {}", data);
    }

    // TODO : 관리자에게 알림을 보내는 메서드 구현 필요
    private void notifyAdministrator(PurchaseData data) {
        // 관리자에게 알림을 보내는 로직
        log.error("Notifying administrator about failed purchase: {}", data);
    }

    //TODO: redis와 재고 동기화
    private void synchronizeTicketStock() {

    }
}