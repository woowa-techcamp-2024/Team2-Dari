package com.wootecam.festivals.global.queue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.entity.TicketStock;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.repository.TicketStockRepository;
import com.wootecam.festivals.global.queue.CustomQueue;
import com.wootecam.festivals.global.queue.InMemoryQueue;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.exception.QueueFullException;
import com.wootecam.festivals.global.utils.TimeProvider;
import com.wootecam.festivals.utils.Fixture;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("QueueService 통합 테스트")
class QueueServiceIntegrationTest extends SpringBootTestConfig {

    @Autowired
    private QueueService queueService;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private FestivalRepository festivalRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TicketStockRepository ticketStockRepository;
    @MockBean
    private TimeProvider timeProvider;

    private Member testMember;
    private Ticket testTicket;
    private Festival testFestival;
    private TicketStock testTicketStock;

    @BeforeEach
    void setUp() {
        clear();
        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
        testMember = memberRepository.saveAndFlush(Fixture.createMember("Test User", "test@example.com"));
        testFestival = festivalRepository.saveAndFlush(
                Fixture.createFestival(testMember, "test", "test", timeProvider.getCurrentTime(),
                        timeProvider.getCurrentTime().plusDays(3)));
        testTicket = ticketRepository.saveAndFlush(
                Fixture.createTicket(testFestival, 10000L, 1000, timeProvider.getCurrentTime().minusDays(2),
                        timeProvider.getCurrentTime().plusDays(1)));
        testTicketStock = ticketStockRepository.saveAndFlush(TicketStock.builder().ticket(testTicket).build());
        resetQueueService();
    }

    private void resetQueueService() {
        ReflectionTestUtils.setField(queueService, "queue", new InMemoryQueue<>(3000));
        ReflectionTestUtils.setField(queueService, "errorQueue", new ConcurrentLinkedQueue<>());
        ReflectionTestUtils.setField(queueService, "retryCount", new ConcurrentHashMap<>());
    }

    @Test
    @DisplayName("스케줄링된 작업이 주기적으로 실행된다")
    void scheduledTasksAreExecutedPeriodically() throws InterruptedException {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(queueService::processPurchases, 0, 1, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(queueService::processErrorQueue, 0, 1, TimeUnit.SECONDS);

        IntStream.range(0, 50).forEach(i ->
                queueService.addPurchase(
                        new PurchaseData(testMember.getId(), testTicket.getId(), testTicketStock.getId()))
        );

        Thread.sleep(3000);  // 스케줄링된 작업이 실행될 시간 대기

        executorService.shutdown();
        List<Purchase> purchases = purchaseRepository.findAll();
        assertThat(purchases).isNotEmpty();
    }

    @Nested
    @DisplayName("addPurchase 메서드는")
    class Describe_addPurchase {

        @Test
        @DisplayName("유효한 구매 데이터를 큐에 추가할 수 있다")
        void it_adds_valid_purchase_data_to_queue() {
            PurchaseData purchaseData = new PurchaseData(testMember.getId(), testTicket.getId(),
                    testTicketStock.getId());
            queueService.addPurchase(purchaseData);
            CustomQueue<PurchaseData> queue = (CustomQueue<PurchaseData>) ReflectionTestUtils.getField(queueService,
                    "queue");
            assertThat(queue.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("큐가 가득 찼을 때 에러 큐에 추가한다")
        void it_adds_to_error_queue_when_main_queue_is_full() {
            CustomQueue<PurchaseData> mockQueue = mock(CustomQueue.class);
            doThrow(new QueueFullException("Queue is full")).when(mockQueue).offer(any());
            ReflectionTestUtils.setField(queueService, "queue", mockQueue);

            PurchaseData purchaseData = new PurchaseData(testMember.getId(), testTicket.getId(),
                    testTicketStock.getId());
            queueService.addPurchase(purchaseData);

            ConcurrentLinkedQueue<PurchaseData> errorQueue = (ConcurrentLinkedQueue<PurchaseData>) ReflectionTestUtils.getField(
                    queueService, "errorQueue");
            assertThat(errorQueue).contains(purchaseData);
        }
    }

    @Nested
    @DisplayName("processPurchases 메서드는")
    class Describe_processPurchases {

        @Test
        @DisplayName("동적으로 배치 크기를 결정해 구매 데이터를 한 번에 처리한다")
        void it_processes_batch_size_of_purchase_data() {
            int queueSize = 150;
            IntStream.range(0, queueSize).forEach(i ->
                    queueService.addPurchase(
                            new PurchaseData(testMember.getId(), testTicket.getId(), testTicketStock.getId()))
            );

            queueService.processPurchases();

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                List<Purchase> purchases = purchaseRepository.findAll();
                assertThat(purchases).hasSizeGreaterThanOrEqualTo(100);
                assertThat(purchases).hasSizeLessThanOrEqualTo(150);
            });
        }

        @Test
        @DisplayName("비동기로 처리되며 예외가 발생해도 다른 처리에 영향을 주지 않는다")
        void it_processes_asynchronously_and_handles_exceptions() {
            CustomQueue<PurchaseData> mockQueue = mock(CustomQueue.class);
            when(mockQueue.pollBatch(anyInt())).thenReturn(
                    List.of(
                            new PurchaseData(testMember.getId(), testTicket.getId(), testTicketStock.getId()),
                            new PurchaseData(-1L, -1L, -1L)  // 잘못된 데이터
                    )
            );
            ReflectionTestUtils.setField(queueService, "queue", mockQueue);

            queueService.processPurchases();

            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                List<Purchase> purchases = purchaseRepository.findAll();
                assertThat(purchases).hasSize(1);
            });
        }
    }

    @Nested
    @DisplayName("processErrorQueue 메서드는")
    class Describe_processErrorQueue {
        @Test
        @DisplayName("최대 재시도 횟수를 초과한 항목은 영구 에러로 처리한다")
        void it_handles_items_exceeding_max_retry_count() {
            ConcurrentLinkedQueue<PurchaseData> errorQueue = (ConcurrentLinkedQueue<PurchaseData>) ReflectionTestUtils.getField(
                    queueService, "errorQueue");
            ConcurrentMap<PurchaseData, Integer> retryCount = (ConcurrentMap<PurchaseData, Integer>) ReflectionTestUtils.getField(
                    queueService, "retryCount");

            PurchaseData errorData = new PurchaseData(-1L, -1L, -1L);  // 잘못된 데이터
            errorQueue.add(errorData);
            retryCount.put(errorData, 3);  // 최대 재시도 횟수 설정

            queueService.processErrorQueue();

            assertThat(errorQueue).isEmpty();
            assertThat(retryCount).doesNotContainKey(errorData);
            // 영구 에러 저장소 확인 로직 추가 (미구현 상태라 주석 처리)
            // assertThat(permanentErrorStorage).contains(errorData);
        }
    }
}