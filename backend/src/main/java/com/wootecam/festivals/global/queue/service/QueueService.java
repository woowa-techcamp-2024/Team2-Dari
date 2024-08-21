package com.wootecam.festivals.global.queue.service;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
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

    // 구매 데이터를 큐에 추가하는 메서드
    public void addPurchase(PurchaseData purchaseData) {
        try {
            queue.offer(purchaseData);
            log.debug("Added purchaseData to InMemoryQueue : {}", purchaseData);
        } catch (QueueFullException e) {
            // 큐가 가득 찼을 경우 에러 큐에 추가
            log.error("Failed to add purchase to queue: {}", purchaseData, e);
            errorQueue.offer(purchaseData);
            log.warn("Purchase data added to error queue: {}", purchaseData);
        }
    }

    // 주기적으로 큐의 구매 데이터를 처리하는 메서드
    @Scheduled(fixedRate = 500) // 500ms마다 실행
    @Transactional
    public void processPurchases() {
        int batchSize = calculateOptimalBatchSize();
        List<PurchaseData> batch = queue.pollBatch(batchSize);
        if (!batch.isEmpty()) {
            try {
                processPurchaseBatch(batch);
            } catch (Exception e) {
                log.error("Failed to process purchase batch: {}", batch, e);
                handleFailedBatch(batch);
            }
        }
    }

    // 배치로 구매 데이터를 처리하는 메서드
    private void processPurchaseBatch(List<PurchaseData> batch) {
        // 티켓과 회원 정보를 한 번에 조회하여 맵으로 저장
        Map<Long, Ticket> ticketMap = fetchTickets(batch);
        Map<Long, Member> memberMap = fetchMembers(batch);

        // 구매 엔티티 생성
        List<Purchase> purchases = batch.stream()
                .map(data -> createPurchase(data, ticketMap.get(data.ticketId()), memberMap.get(data.memberId())))
                .collect(Collectors.toList());

        // JDBC 배치 업데이트를 사용하여 대량 삽입 수행
        jdbcTemplate.batchUpdate(
                "INSERT INTO purchase (ticket_id, member_id, purchase_time, purchase_status) VALUES (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Purchase purchase = purchases.get(i);
                        ps.setLong(1, purchase.getTicket().getId());
                        ps.setLong(2, purchase.getMember().getId());
                        ps.setTimestamp(3, Timestamp.valueOf(purchase.getPurchaseTime()));
                        ps.setString(4, purchase.getPurchaseStatus().name());
                    }

                    @Override
                    public int getBatchSize() {
                        return purchases.size();
                    }
                }
        );

        log.debug("Processed {} purchases", purchases.size());
    }

    // 구매 엔티티를 생성하는 메서드
    private Purchase createPurchase(PurchaseData purchaseData, Ticket ticket, Member member) {
        if (ticket == null || member == null) {
            log.error("Invalid purchase data : {}", purchaseData);
            throw new IllegalArgumentException("Invalid purchase data: " + purchaseData);
        }
        return Purchase.builder()
                .ticket(ticket)
                .member(member)
                .purchaseTime(timeProvider.getCurrentTime())
                .purchaseStatus(PurchaseStatus.PURCHASED)
                .build();
    }

    // 배치의 티켓 정보를 한 번에 조회하는 메서드
    private Map<Long, Ticket> fetchTickets(List<PurchaseData> batch) {
        List<Long> ticketIds = batch.stream().map(PurchaseData::ticketId).distinct().collect(Collectors.toList());
        return ticketRepository.findAllById(ticketIds).stream()
                .collect(Collectors.toMap(Ticket::getId, ticket -> ticket));
    }

    // 배치의 회원 정보를 한 번에 조회하는 메서드
    private Map<Long, Member> fetchMembers(List<PurchaseData> batch) {
        List<Long> memberIds = batch.stream().map(PurchaseData::memberId).distinct().collect(Collectors.toList());
        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));
    }

    // 큐의 크기에 따라 최적의 배치 크기를 계산하는 메서드
    private int calculateOptimalBatchSize() {
        int queueSize = queue.size();
        return Math.min(Math.max(queueSize / 10, 10), 1000);
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
        Ticket ticket = ticketRepository.findById(data.ticketId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticket ID"));
        Member member = memberRepository.findById(data.memberId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        Purchase purchase = createPurchase(data, ticket, member);
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
            saveToPerminantErrorStorage(data);
            notifyAdministrator(data);
            retryCount.remove(data);
        }
    }

    // TODO: 영구 에러 저장소에 저장하는 메서드 구현 필요
    private void saveToPerminantErrorStorage(PurchaseData data) {
        // 영구 저장소(예: 데이터베이스의 별도 테이블)에 저장하는 로직
        log.error("Saving failed purchase to permanent error storage: {}", data);
    }

    // TODO : 관리자에게 알림을 보내는 메서드 구현 필요
    private void notifyAdministrator(PurchaseData data) {
        // 관리자에게 알림을 보내는 로직 (이메일, SMS 등)
        log.error("Notifying administrator about failed purchase: {}", data);
    }
}