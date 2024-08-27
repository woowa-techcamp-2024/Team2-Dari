package com.wootecam.festivals.global.queue.service;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.service.TicketCacheService;
import com.wootecam.festivals.global.queue.CustomQueue;
import com.wootecam.festivals.global.queue.InMemoryQueue;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.exception.QueueFullException;
import com.wootecam.festivals.global.utils.TimeProvider;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
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
    private final TicketCacheService ticketCacheService;

    // 동적으로 스레드풀을 생성하도록 변경
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2, // 코어 스레드 수
            Runtime.getRuntime().availableProcessors(), // 최대 스레드 수
            60L, TimeUnit.SECONDS, // 유휴 스레드 대기 시간
            new LinkedBlockingQueue<Runnable>(100), // 작업 큐
            new ThreadPoolExecutor.CallerRunsPolicy() // 거부 정책
    );

    // 구매 데이터를 큐에 추가하는 메서드
    public void addPurchase(PurchaseData purchaseData) {
        if (purchaseData == null) {
            throw new IllegalArgumentException("Purchase data cannot be null");
        }
        try {
            queue.offer(purchaseData);
            log.debug("ADD,{}", purchaseData);
        } catch (QueueFullException e) {
            log.error("QUEUE_FULL,{}", purchaseData, e);
            errorQueue.offer(purchaseData);
            log.warn("ERROR_QUEUE_ADD,{}", purchaseData);
        }
    }

    // 주기적으로 큐의 구매 데이터를 처리하는 메서드
    @Scheduled(fixedRate = 5000) // 5초마다 실행
    public void processPurchases() {
        int batchSize = calculateOptimalBatchSize();
        List<PurchaseData> batch = queue.pollBatch(batchSize);
        if (!batch.isEmpty()) {
            CompletableFuture.runAsync(() -> processBatch(batch), executor)
                    .exceptionally(e -> {
                        log.error("BATCH_PROCESS_FAIL,size={}", batch.size(), e);
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
                log.debug("PURCHASE_SUCCESS,{}", purchase);
            } catch (Exception e) {
                log.error("PURCHASE_FAIL,{}", purchase, e);
            }
        }

        if (!successfulPurchases.isEmpty()) {
            batchInsertPurchases(successfulPurchases);
            batchInsertCheckins(successfulPurchases);
            synchronizeTicketStock();
            log.debug("BATCH_INSERT_SUCCESS,size={}", successfulPurchases.size());
        }
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
            log.debug("Processing {} items from error queue", errorBatch.size());
            for (PurchaseData data : errorBatch) {
                try {
                    processSinglePurchase(data);
                    log.debug("Successfully processed error item: {}", data);
                } catch (Exception e) {
                    handleRetry(data);
                }
            }
        }
    }

    // 구매 엔티티를 생성하는 메서드
    private Purchase createPurchase(PurchaseData purchaseData) {
        Ticket ticket = ticketCacheService.getTicket(purchaseData.ticketId());
        Member member = getMemberFromDatabase(purchaseData.memberId());
        return Purchase.builder()
                .ticket(ticket)
                .member(member)
                .purchaseTime(timeProvider.getCurrentTime())
                .purchaseStatus(PurchaseStatus.PURCHASED)
                .build();
    }


    private Checkin createCheckin(Purchase purchase) {
        return Checkin.builder()
                .member(purchase.getMember())
                .ticket(purchase.getTicket())
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

    // 단일 구매 데이터를 처리하는 메서드
    private void processSinglePurchase(PurchaseData data) {
        Purchase purchase = createPurchase(data);
        purchaseRepository.save(purchase);
    }

    // 재시도 로직을 처리하는 메서드
    private void handleRetry(PurchaseData data) {
        int count = retryCount.compute(data, (k, v) -> v == null ? 1 : v + 1);
        if (count <= MAX_RETRY_COUNT) {
            log.warn("RETRY,attempt={},data={}", count, data);
            errorQueue.offer(data);
        } else {
            log.error("MAX_RETRY_REACHED,data={}", data);
            retryCount.remove(data);
        }
    }

    //TODO: redis와 재고 동기화
    private void synchronizeTicketStock() {

    }

    @PostConstruct
    public void recoverQueue() {
        log.debug("Starting queue recovery process");
        Set<PurchaseData> addedPurchases = new HashSet<>();
        Set<PurchaseData> completedPurchases = new HashSet<>();

        try {
            File logDir = new File("logs");
            File[] logFiles = logDir.listFiles(
                    (dir, name) -> name.startsWith("queue-service") && name.endsWith(".log"));
            if (logFiles != null) {
                Arrays.sort(logFiles, Comparator.comparing(File::lastModified).reversed());

                for (File logFile : logFiles) {
                    processLogFile(logFile, addedPurchases, completedPurchases);
                    if (addedPurchases.size() > completedPurchases.size()) {
                        break; // 모든 미처리 구매를 찾았으므로 중단
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during queue recovery", e);
        }

        // 미처리 구매 요청을 다시 큐에 추가
        addedPurchases.removeAll(completedPurchases);
        for (PurchaseData purchase : addedPurchases) {
            try {
                queue.offer(purchase);
                log.debug("Recovered purchase added to queue: {}", purchase);
            } catch (QueueFullException e) {
                log.error("Failed to recover purchase, queue is full: {}", purchase);
            }
        }

        log.debug("Queue recovery process completed. Recovered {} purchases", addedPurchases.size());
    }

    private void processLogFile(File logFile, Set<PurchaseData> addedPurchases, Set<PurchaseData> completedPurchases) {
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("ADD,")) {
                    PurchaseData purchase = parsePurchaseData(line);
                    addedPurchases.add(purchase);
                } else if (line.contains("PURCHASE_SUCCESS,")) {
                    PurchaseData purchase = parsePurchaseData(line);
                    completedPurchases.add(purchase);
                }
            }
        } catch (IOException e) {
            log.error("Error reading log file: {}", logFile.getName(), e);
        }
    }

    private PurchaseData parsePurchaseData(String logLine) {
        // 로그 라인에서 PurchaseData 정보 추출
        String[] parts = logLine.split("PurchaseData\\[")[1].split("\\]")[0].split(", ");
        long memberId = Long.parseLong(parts[0].split("=")[1]);
        long ticketId = Long.parseLong(parts[1].split("=")[1]);
        long ticketStockId = Long.parseLong(parts[2].split("=")[1]);
        return new PurchaseData(memberId, ticketId, ticketStockId);
    }

}