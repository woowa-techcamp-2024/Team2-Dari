package com.wootecam.festivals.global.queue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.domain.ticket.service.TicketCacheService;
import com.wootecam.festivals.global.queue.CustomQueue;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.queue.exception.QueueFullException;
import com.wootecam.festivals.global.utils.TimeProvider;
import com.wootecam.festivals.utils.Fixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private TicketCacheService ticketCacheService;
    @Mock
    private TimeProvider timeProvider;

    private QueueService queueService;
    private Ticket ticket;
    private Member member;
    private Festival festival;

    @BeforeEach
    void setUp() {
        queueService = new QueueService(purchaseRepository, ticketRepository, memberRepository, timeProvider,
                jdbcTemplate, ticketCacheService);
        member = Fixture.createMember("Test User", "test@example.com");
        festival = Fixture.createFestival(member, "Test Festival", "Description", LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(5));
        ticket = Fixture.createTicket(festival, 1000L, 10, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(3));
        lenient().when(timeProvider.getCurrentTime()).thenReturn(LocalDateTime.now());
    }

    @Test
    @DisplayName("동시성 테스트: 여러 스레드에서 동시에 구매를 추가한다")
    void concurrencyTest() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long id = i;
            executorService.submit(() -> {
                try {
                    queueService.addPurchase(new PurchaseData(id, id, id));
                } finally {
                    latch.countDown();
                }
            });
        }

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        executorService.shutdown();

        CustomQueue<PurchaseData> queue = (CustomQueue<PurchaseData>) ReflectionTestUtils.getField(queueService,
                "queue");
        assertThat(queue.size()).isEqualTo(threadCount);
    }

    @Nested
    @DisplayName("addPurchase 메소드")
    class AddPurchaseTest {

        @Test
        @DisplayName("유효한 구매 데이터를 추가할 수 있다")
        void shouldAddValidPurchaseData() {
            PurchaseData purchaseData = new PurchaseData(1L, 1L, 1L);
            assertThatCode(() -> queueService.addPurchase(purchaseData)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null 구매 데이터를 추가하면 예외가 발생한다")
        void shouldThrowExceptionForNullPurchaseData() {
            assertThatThrownBy(() -> queueService.addPurchase(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Purchase data cannot be null");
        }

        @Test
        @DisplayName("큐가 가득 찼을 때 에러 큐에 추가된다")
        void shouldAddToErrorQueueWhenMainQueueIsFull() throws Exception {
            CustomQueue<PurchaseData> mockQueue = mock(CustomQueue.class);
            doThrow(new QueueFullException("Queue is full")).when(mockQueue).offer(any());
            ReflectionTestUtils.setField(queueService, "queue", mockQueue);

            PurchaseData purchaseData = new PurchaseData(1L, 1L, 1L);
            queueService.addPurchase(purchaseData);

            verify(mockQueue).offer(purchaseData);
        }
    }

    @Nested
    @DisplayName("processBatch 메소드")
    class ProcessBatchTest {

        @Test
        @DisplayName("배치로 구매 처리를 수행한다")
        void shouldProcessBatchOfPurchases() {
            List<PurchaseData> batch = List.of(
                    new PurchaseData(1L, 1L, 1L),
                    new PurchaseData(2L, 2L, 2L)
            );

            when(ticketCacheService.getTicket(anyLong())).thenReturn(ticket);
            when(memberRepository.getReferenceById(anyLong())).thenReturn(member);

            ReflectionTestUtils.invokeMethod(queueService, "processBatch", batch);

            verify(jdbcTemplate, times(2)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }
    }

    @Nested
    @DisplayName("processErrorQueue 메소드")
    class ProcessErrorQueueTest {

        @Test
        @DisplayName("에러 큐의 항목을 처리한다")
        void shouldProcessItemsInErrorQueue() {
            PurchaseData errorData = new PurchaseData(1L, 1L, 1L);
            ConcurrentLinkedQueue<PurchaseData> errorQueue = new ConcurrentLinkedQueue<>();
            errorQueue.offer(errorData);
            ReflectionTestUtils.setField(queueService, "errorQueue", errorQueue);

            when(ticketCacheService.getTicket(anyLong())).thenReturn(ticket);
            when(memberRepository.getReferenceById(anyLong())).thenReturn(member);
            when(purchaseRepository.save(any(Purchase.class))).thenReturn(Purchase.builder()
                    .member(member)
                    .ticket(ticket)
                    .purchaseTime(LocalDateTime.now())
                    .purchaseStatus(PurchaseStatus.PURCHASED)
                    .build());

            queueService.processErrorQueue();

            verify(purchaseRepository).save(any(Purchase.class));
        }
    }

    @Nested
    @DisplayName("createPurchase 메소드")
    class CreatePurchaseTest {

        @Test
        @DisplayName("구매 엔티티를 생성한다")
        void shouldCreatePurchaseEntity() {
            PurchaseData purchaseData = new PurchaseData(1L, 1L, 1L);
            when(ticketCacheService.getTicket(anyLong())).thenReturn(ticket);
            when(memberRepository.getReferenceById(anyLong())).thenReturn(member);

            Purchase purchase = ReflectionTestUtils.invokeMethod(queueService, "createPurchase", purchaseData);

            assertThat(purchase).isNotNull();
            assertThat(purchase.getTicket()).isEqualTo(ticket);
            assertThat(purchase.getMember()).isEqualTo(member);
            assertThat(purchase.getPurchaseStatus()).isEqualTo(PurchaseStatus.PURCHASED);
        }
    }

    @Nested
    @DisplayName("batchInsertPurchases 메소드")
    class BatchInsertPurchasesTest {

        @Test
        @DisplayName("구매 정보를 벌크 인서트한다")
        void shouldBatchInsertPurchases() {
            List<Purchase> purchases = List.of(
                    Purchase.builder().ticket(ticket).member(member).purchaseTime(LocalDateTime.now())
                            .purchaseStatus(PurchaseStatus.PURCHASED).build(),
                    Purchase.builder().ticket(ticket).member(member).purchaseTime(LocalDateTime.now())
                            .purchaseStatus(PurchaseStatus.PURCHASED).build()
            );

            ReflectionTestUtils.invokeMethod(queueService, "batchInsertPurchases", purchases);

            verify(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }
    }

    @Nested
    @DisplayName("batchInsertCheckins 메소드")
    class BatchInsertCheckinsTest {

        @Test
        @DisplayName("체크인 정보를 벌크 인서트한다")
        void shouldBatchInsertCheckins() {
            List<Purchase> purchases = List.of(
                    Purchase.builder().ticket(ticket).member(member).purchaseTime(LocalDateTime.now())
                            .purchaseStatus(PurchaseStatus.PURCHASED).build(),
                    Purchase.builder().ticket(ticket).member(member).purchaseTime(LocalDateTime.now())
                            .purchaseStatus(PurchaseStatus.PURCHASED).build()
            );

            ReflectionTestUtils.invokeMethod(queueService, "batchInsertCheckins", purchases);

            verify(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }
    }
}