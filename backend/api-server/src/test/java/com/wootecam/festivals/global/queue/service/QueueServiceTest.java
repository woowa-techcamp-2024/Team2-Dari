package com.wootecam.festivals.global.queue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.queue.CustomQueue;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.utils.TimeProvider;
import com.wootecam.festivals.utils.Fixture;
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

    private TimeProvider timeProvider;
    private QueueService queueService;
    private Ticket ticket;
    private Member member;

    @BeforeEach
    void setUp() {
        timeProvider = new TimeProvider();
        queueService = new QueueService(purchaseRepository, ticketRepository, memberRepository, timeProvider,
                jdbcTemplate);
        member = Fixture.createMember("Test User", "test@example.com");
        Festival festival = Fixture.createFestival(member, "test", "test", timeProvider.getCurrentTime(),
                timeProvider.getCurrentTime().plusDays(3));
        ticket = Fixture.createTicket(festival, 10000L, 1000, timeProvider.getCurrentTime().minusDays(2),
                timeProvider.getCurrentTime().plusDays(1));
    }

    @Nested
    @DisplayName("구매 데이터 추가 시")
    class AddPurchaseTest {

        @Test
        @DisplayName("정상적인 데이터라면 예외 없이 추가된다")
        void shouldAddPurchaseSuccessfully() {
            PurchaseData purchaseData = new PurchaseData(1L, 1L);
            assertThatCode(() -> queueService.addPurchase(purchaseData))
                    .doesNotThrowAnyException();
        }

        @Nested
        @DisplayName("유효한 구매 데이터가 주어졌을 때")
        class Context_with_valid_purchase_data {

            @Test
            @DisplayName("큐에 성공적으로 데이터를 추가한다")
            void it_adds_data_to_queue_successfully() {
                // Given
                PurchaseData purchaseData = new PurchaseData(1L, 1L);

                // When
                queueService.addPurchase(purchaseData);

                // Then
                CustomQueue<PurchaseData> queue = (CustomQueue<PurchaseData>) ReflectionTestUtils.getField(queueService,
                        "queue");
                assertThat(queue.size()).isEqualTo(1);
            }
        }

        @Nested
        @DisplayName("null 데이터가 주어졌을 때")
        class Context_with_null_data {

            @Test
            @DisplayName("IllegalArgumentException을 던진다")
            void it_throws_IllegalArgumentException() {
                // When & Then
                assertThatThrownBy(() -> queueService.addPurchase(null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Purchase data cannot be null");
            }
        }

        @Nested
        @DisplayName("큐가 가득 찼을 때")
        class Context_when_queue_is_full {

            @Test
            @DisplayName("에러 큐에 데이터를 추가한다")
            void it_adds_data_to_error_queue() {
                CustomQueue<PurchaseData> fullQueue = mock(CustomQueue.class);

                QueueService queueServiceWithFullQueue = new QueueService(purchaseRepository, ticketRepository,
                        memberRepository, timeProvider, jdbcTemplate) {
                    protected CustomQueue<PurchaseData> createQueue() {
                        return fullQueue;
                    }
                };

                PurchaseData purchaseData = new PurchaseData(1L, 1L);
                assertThatCode(() -> queueServiceWithFullQueue.addPurchase(purchaseData))
                        .doesNotThrowAnyException();
            }
        }

        @Test
        @DisplayName("여러 스레드에서 동시에 추가해도 안전하게 처리된다")
        void shouldHandleConcurrentAdditions() throws InterruptedException {
            int threadCount = 100;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final long id = i;
                executorService.submit(() -> {
                    try {
                        queueService.addPurchase(new PurchaseData(id, id));
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
            executorService.shutdown();
        }
    }

    @Nested
    @DisplayName("processPurchases 메소드는")
    class ProcessPurchasesTest {

        @Test
        @DisplayName("정상적인 구매 데이터라면 성공적으로 처리된다")
        void shouldProcessPurchasesSuccessfully() {
            PurchaseData purchaseData = new PurchaseData(1L, 1L);
            queueService.addPurchase(purchaseData);

            when(ticketRepository.getReferenceById(1L)).thenReturn(ticket);
            when(memberRepository.getReferenceById(1L)).thenReturn(member);

            queueService.processPurchases();

            verify(jdbcTemplate, times(2)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }

        @Test
        @DisplayName("큐가 비어있다면 아무 작업도 수행하지 않는다")
        void shouldDoNothingWhenQueueIsEmpty() {
            queueService.processPurchases();
            verify(jdbcTemplate, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }

        @Test
        @DisplayName("일부 구매 실패 시 성공한 구매만 처리한다")
        void shouldProcessOnlySuccessfulPurchases() {
            PurchaseData successData = new PurchaseData(1L, 1L);
            PurchaseData failData = new PurchaseData(2L, 2L);
            queueService.addPurchase(successData);
            queueService.addPurchase(failData);

            when(ticketRepository.getReferenceById(1L)).thenReturn(ticket);
            when(memberRepository.getReferenceById(1L)).thenReturn(member);
            when(ticketRepository.getReferenceById(2L)).thenThrow(new RuntimeException("Ticket not found"));

            queueService.processPurchases();

            verify(jdbcTemplate, times(2)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }

        @Test
        @DisplayName("대량의 구매 데이터도 정상적으로 처리한다")
        void shouldHandleBulkPurchases() {
            int purchaseCount = 1000;
            for (int i = 0; i < purchaseCount; i++) {
                queueService.addPurchase(new PurchaseData((long) i, (long) i));
            }

            when(ticketRepository.getReferenceById(anyLong())).thenReturn(ticket);
            when(memberRepository.getReferenceById(anyLong())).thenReturn(member);

            queueService.processPurchases();

            verify(jdbcTemplate, atLeastOnce()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
        }

        @Nested
        @DisplayName("큐에 구매 데이터가 있을 때")
        class Context_with_purchase_data_in_queue {

            @BeforeEach
            void setUp() {
                lenient().when(ticketRepository.getReferenceById(anyLong())).thenReturn(ticket);
                lenient().when(memberRepository.getReferenceById(anyLong())).thenReturn(member);
            }

            @Test
            @DisplayName("구매 데이터를 처리하고 데이터베이스에 저장한다")
            void it_processes_data_and_saves_to_database() {
                // Given
                PurchaseData purchaseData = new PurchaseData(1L, 1L);
                queueService.addPurchase(purchaseData);

                // When
                queueService.processPurchases();

                // Then
                verify(jdbcTemplate, times(2)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
            }

            @Nested
            @DisplayName("에러 큐 처리 시")
            class ProcessErrorQueueTest {

                @Test
                @DisplayName("재시도 횟수를 초과하면 영구 오류 저장소에 저장한다")
                void shouldSaveToPermanentErrorStorageAfterMaxRetries() {
                    PurchaseData errorData = new PurchaseData(1L, 1L);
                    queueService.addPurchase(errorData);

                    int maxRetryCount = 3;
                    for (int i = 0; i <= maxRetryCount; i++) {
                        queueService.processErrorQueue();
                    }

                    verify(purchaseRepository, never()).save(any(Purchase.class));
                    // TODO: 영구 오류 저장소 저장 검증 로직 추가
                }
            }


            @Nested
            @DisplayName("동시성 처리 시")
            class ConcurrencyTest {

                @Test
                @DisplayName("여러 스레드에서 동시에 추가 및 처리해도 안전하게 동작한다")
                void shouldHandleConcurrentAdditionsAndProcessing() throws InterruptedException {
                    int threadCount = 10;
                    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
                    CountDownLatch addLatch = new CountDownLatch(threadCount);
                    CountDownLatch processLatch = new CountDownLatch(threadCount);

                    when(ticketRepository.getReferenceById(anyLong())).thenReturn(ticket);
                    when(memberRepository.getReferenceById(anyLong())).thenReturn(member);

                    for (int i = 0; i < threadCount; i++) {
                        final long id = i;
                        executorService.submit(() -> {
                            try {
                                queueService.addPurchase(new PurchaseData(id, id));
                            } finally {
                                addLatch.countDown();
                            }
                        });

                        executorService.submit(() -> {
                            try {
                                queueService.processPurchases();
                            } finally {
                                processLatch.countDown();
                            }
                        });
                    }

                    assertThat(addLatch.await(10, TimeUnit.SECONDS)).isTrue();
                    assertThat(processLatch.await(10, TimeUnit.SECONDS)).isTrue();
                    executorService.shutdown();

                    verify(jdbcTemplate, atLeastOnce()).batchUpdate(anyString(),
                            any(BatchPreparedStatementSetter.class));
                }
            }
        }
    }
}