package com.wootecam.festivals.global.queue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.queue.CustomQueue;
import com.wootecam.festivals.global.queue.InMemoryQueue;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.utils.TimeProvider;
import com.wootecam.festivals.utils.Fixture;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("QueueService 통합 테스트")
class QueueServiceTest extends SpringBootTestConfig {

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

    @MockBean
    private TimeProvider timeProvider;

    private Member testMember;
    private Ticket testTicket;
    private Festival testFestival;

    @BeforeEach
    void setUp() {
        clear();
        when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
        Member member = Fixture.createMember("Test User", "test@example.com");
        testMember = memberRepository.save(member);
        Festival festival = Fixture.createFestival(testMember, "test", "test", timeProvider.getCurrentTime(),
                timeProvider.getCurrentTime().plusDays(3));
        testFestival = festivalRepository.save(festival);
        Ticket ticket = Fixture.createTicket(testFestival, 10000L, 1000, timeProvider.getCurrentTime().minusDays(2),
                timeProvider.getCurrentTime().plusDays(1));
        testTicket = ticketRepository.save(ticket);

        resetQueueService();
    }

    private void resetQueueService() {
        CustomQueue<PurchaseData> queue = new InMemoryQueue<>(3000);
        ConcurrentLinkedQueue<PurchaseData> errorQueue = new ConcurrentLinkedQueue<>();
        ConcurrentMap<PurchaseData, Integer> retryCount = new ConcurrentHashMap<>();

        ReflectionTestUtils.setField(queueService, "queue", queue);
        ReflectionTestUtils.setField(queueService, "errorQueue", errorQueue);
        ReflectionTestUtils.setField(queueService, "retryCount", retryCount);
    }

    @Nested
    @DisplayName("addPurchase 메서드는")
    class Describe_addPurchase {

        @Test
        @DisplayName("유효한 구매 데이터를 큐에 추가할 수 있다")
        void it_adds_valid_purchase_data_to_queue() throws Exception {
            // Given
            PurchaseData purchaseData = new PurchaseData(testMember.getId(), testTicket.getId());

            // When
            queueService.addPurchase(purchaseData);

            // Then
            CustomQueue<PurchaseData> queue = (CustomQueue<PurchaseData>) ReflectionTestUtils.getField(queueService,
                    "queue");
            assertThat(queue.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("큐가 가득 찼을 때 에러 큐에 추가한다")
        void it_adds_to_error_queue_when_main_queue_is_full() throws Exception {
            // Given
            int queueSize = (int) ReflectionTestUtils.getField(queueService, "QUEUE_SIZE");

            // 메인 큐를 가득 채움
            for (int i = 0; i < queueSize; i++) {
                PurchaseData data = new PurchaseData(testMember.getId(), testTicket.getId());
                queueService.addPurchase(data);
            }

            // When
            PurchaseData overflowData = new PurchaseData(testMember.getId(), testTicket.getId());
            queueService.addPurchase(overflowData);

            // Then
            CustomQueue<PurchaseData> mainQueue = (CustomQueue<PurchaseData>) ReflectionTestUtils.getField(queueService, "queue");
            ConcurrentLinkedQueue<PurchaseData> errorQueue = (ConcurrentLinkedQueue<PurchaseData>) ReflectionTestUtils.getField(queueService, "errorQueue");

            assertThat(mainQueue.size()).isEqualTo(queueSize);
            assertThat(errorQueue).hasSize(1);
            assertThat(errorQueue.peek()).isEqualTo(overflowData);
        }
    }

    @Nested
    @DisplayName("processPurchases 메서드는")
    class Describe_processPurchases {

        @Test
        @DisplayName("큐에 있는 구매 데이터를 처리하고 데이터베이스에 저장한다")
        void it_processes_purchase_data_and_saves_to_database() {
            // Given
            PurchaseData purchaseData = new PurchaseData(testMember.getId(), testTicket.getId());
            queueService.addPurchase(purchaseData);

            // When
            queueService.processPurchases();

            // Then
            List<Purchase> purchases = purchaseRepository.findAll();
            assertThat(purchases).hasSize(1);
            Purchase savedPurchase = purchases.get(0);

            assertThat(savedPurchase.getMember().getId()).isEqualTo(testMember.getId());
            assertThat(savedPurchase.getMember().getName()).isEqualTo(testMember.getName());
            assertThat(savedPurchase.getMember().getEmail()).isEqualTo(testMember.getEmail());

            assertThat(savedPurchase.getTicket().getId()).isEqualTo(testTicket.getId());
            assertThat(savedPurchase.getTicket().getName()).isEqualTo(testTicket.getName());
        }

        @Test
        @DisplayName("배치 크기만큼의 구매 데이터를 한 번에 처리한다")
        void it_processes_batch_size_of_purchase_data() throws Exception {
            // Given
            int batchSize = 10;
            IntStream.range(0, batchSize * 2).forEach(i ->
                    queueService.addPurchase(new PurchaseData(testMember.getId(), testTicket.getId()))
            );

            // When
            queueService.processPurchases();

            // Then
            List<Purchase> purchases = purchaseRepository.findAll();
            assertThat(purchases).hasSize(batchSize);
        }
    }

    @Nested
    @DisplayName("processErrorQueue 메서드는")
    class Describe_processErrorQueue {

        @Test
        @DisplayName("에러 큐의 항목을 처리하고 성공적으로 데이터베이스에 저장한다")
        void it_processes_error_queue_items_and_saves_to_database() throws Exception {
            // Given
            ConcurrentLinkedQueue<PurchaseData> errorQueue = (ConcurrentLinkedQueue<PurchaseData>) ReflectionTestUtils.getField(
                    queueService, "errorQueue");
            PurchaseData errorData = new PurchaseData(testMember.getId(), testTicket.getId());
            errorQueue.add(errorData);

            // When
            queueService.processErrorQueue();

            // Then
            List<Purchase> purchases = purchaseRepository.findAll();
            assertThat(purchases).hasSize(1);
            Purchase savedPurchase = purchases.get(0);

            assertThat(savedPurchase.getMember().getId()).isEqualTo(testMember.getId());
            assertThat(savedPurchase.getMember().getName()).isEqualTo(testMember.getName());
            assertThat(savedPurchase.getMember().getEmail()).isEqualTo(testMember.getEmail());

            assertThat(savedPurchase.getTicket().getId()).isEqualTo(testTicket.getId());
            assertThat(savedPurchase.getTicket().getName()).isEqualTo(testTicket.getName());
        }
    }
}