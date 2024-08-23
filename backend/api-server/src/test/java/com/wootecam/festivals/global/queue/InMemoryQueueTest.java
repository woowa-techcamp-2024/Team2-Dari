package com.wootecam.festivals.global.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wootecam.festivals.global.queue.exception.QueueFullException;
import com.wootecam.festivals.global.queue.exception.QueueOperationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InMemoryQueueTest {

    // Data
    private final int capacity = 10;
    private final InMemoryQueue<Integer> queue = new InMemoryQueue<>(capacity);

    @Nested
    @DisplayName("큐 기본 동작 테스트")
    class BasicOperationTest {

        @Test
        @DisplayName("기본 생성자 테스트")
        void testDefaultConstructor() {
            InMemoryQueue<String> defaultQueue = new InMemoryQueue<>();
            assertTrue(defaultQueue.isEmpty(), "기본 생성자로 생성된 큐는 비어있어야 합니다.");
            assertEquals(0, defaultQueue.size(), "기본 생성자로 생성된 큐의 초기 크기는 0이어야 합니다.");
        }

        @Test
        @DisplayName("isEmpty 메소드 테스트")
        void testIsEmpty() {
            InMemoryQueue<Integer> queue = new InMemoryQueue<>(5);
            assertTrue(queue.isEmpty(), "새로 생성된 큐는 비어있어야 합니다.");

            queue.offer(1);
            assertFalse(queue.isEmpty(), "요소가 추가된 후 큐는 비어있지 않아야 합니다.");

            queue.poll();
            assertTrue(queue.isEmpty(), "모든 요소가 제거된 후 큐는 다시 비어있어야 합니다.");
        }

        @Test
        @DisplayName("size 메소드 정확성 테스트")
        void testSizeAccuracy() {
            InMemoryQueue<Integer> queue = new InMemoryQueue<>(10);
            assertEquals(0, queue.size(), "새로 생성된 큐의 크기는 0이어야 합니다.");

            for (int i = 0; i < 5; i++) {
                queue.offer(i);
                assertEquals(i + 1, queue.size(), "요소 추가 후 큐의 크기가 정확해야 합니다.");
            }

            for (int i = 5; i > 0; i--) {
                queue.poll();
                assertEquals(i - 1, queue.size(), "요소 제거 후 큐의 크기가 정확해야 합니다.");
            }
        }

        @Test
        @DisplayName("clear 메소드 테스트")
        void testClear() {
            InMemoryQueue<Integer> queue = new InMemoryQueue<>(5);
            for (int i = 0; i < 5; i++) {
                queue.offer(i);
            }
            assertFalse(queue.isEmpty(), "요소 추가 후 큐는 비어있지 않아야 합니다.");

            queue.clear();
            assertTrue(queue.isEmpty(), "clear 후 큐는 비어있어야 합니다.");
            assertEquals(0, queue.size(), "clear 후 큐의 크기는 0이어야 합니다.");
        }


        @Test
        @DisplayName("offer 메소드에서 QueueOperationException 발생 테스트")
        void testOfferThrowsQueueOperationException() {
            // Given
            InMemoryQueue<Integer> smallQueue = new InMemoryQueue<>(1); // 작은 용량의 큐
            smallQueue.offer(1); // 큐를 꽉 채워서 용량을 초과하도록 설정

            AtomicBoolean interrupted = new AtomicBoolean(false);

            // When
            Thread testThread = new Thread(() -> {
                try {
                    // 큐의 용량을 초과하는 상황에서, 강제로 인터럽트를 발생시킵니다.
                    Thread.currentThread().interrupt();
                    smallQueue.offer(2); // 여기서 QueueOperationException이 발생해야 합니다.
                } catch (QueueFullException e) {
                    // 예상되는 예외가 아님
                } catch (QueueOperationException e) {
                    // 예상되는 예외
                    interrupted.set(true);
                }
            });

            testThread.start();

            try {
                testThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Then
            assertTrue(interrupted.get(), "offer 작업 중 QueueOperationException이 발생해야 합니다.");
        }
    }
    @Nested
    @DisplayName("동시성을 고려한 offer 동작 테스트")
    class OfferOperationTest {

        @Test
        @DisplayName("큐에 정상적으로 항목 추가")
        void testOfferWithConcurrency() throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(10);
            List<Exception> exceptions = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                final int item = i;
                executorService.submit(() -> {
                    try {
                        queue.offer(item);
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // Then
            assertEquals(10, queue.size(), "동시성 offer 작업 후 큐에는 10개의 항목이 있어야 합니다.");
            assertTrue(exceptions.isEmpty(), "동시성 offer 작업 중 예외가 발생하지 않아야 합니다.");
        }

        @Test
        @DisplayName("큐 용량 초과 시 예외 발생")
        void testOfferBeyondCapacity() throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(15);
            CountDownLatch latch = new CountDownLatch(15);
            List<Exception> exceptions = new ArrayList<>();

            for (int i = 0; i < 15; i++) {
                final int item = i;
                executorService.submit(() -> {
                    try {
                        queue.offer(item);
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            assertEquals(10, queue.size(), "용량을 초과한 후에도 큐에는 최대 10개의 항목만 있어야 합니다.");
            assertTrue(exceptions.stream().anyMatch(e -> e instanceof QueueFullException),
                    "큐 용량을 초과한 항목에 대해 QueueFullException이 발생해야 합니다.");
        }
    }

    @Nested
    @DisplayName("동시성을 고려한 poll 동작 테스트")
    class PollOperationTest {

        @Test
        @DisplayName("큐에서 항목 정상적으로 제거")
        void testPollWithConcurrency() throws InterruptedException {
            for (int i = 0; i < capacity; i++) {
                queue.offer(i);
            }

            ExecutorService executorService = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(10);
            List<Integer> polledItems = new ArrayList<>();
            List<Exception> exceptions = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                executorService.submit(() -> {
                    try {
                        Integer item = queue.poll();
                        if (item != null) {
                            synchronized (polledItems) {
                                polledItems.add(item);
                            }
                        }
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // Then
            assertEquals(0, queue.size(), "동시성 poll 작업 후 큐는 비어 있어야 합니다.");
            assertEquals(10, polledItems.size(), "큐에서 10개의 항목이 정상적으로 꺼내져야 합니다.");
            assertTrue(exceptions.isEmpty(), "동시성 poll 작업 중 예외가 발생하지 않아야 합니다.");
        }
    }

    @Nested
    @DisplayName("지수 백오프 테스트")
    class ExponentialBackoffTest {

        @Test
        @DisplayName("큐 용량 초과 시 지수 백오프 동작 테스트")
        void testExponentialBackoff() throws InterruptedException {
            // Data (Given)
            InMemoryQueue<Integer> smallQueue = new InMemoryQueue<>(1); // 작은 용량의 큐
            smallQueue.offer(1); // 큐를 꽉 채워서 용량을 초과하도록 설정

            // Context
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            CountDownLatch latch = new CountDownLatch(1);
            List<Long> backoffTimes = new ArrayList<>();
            long startTime = System.currentTimeMillis();

            // Interaction (When)
            executorService.submit(() -> {
                try {
                    smallQueue.offer(2);
                } catch (QueueFullException e) {
                    // 예상대로 예외 발생
                } catch (QueueOperationException e) {
                    e.printStackTrace();
                } finally {
                    long endTime = System.currentTimeMillis();
                    backoffTimes.add(endTime - startTime);
                    latch.countDown();
                }
            });

            latch.await();
            executorService.shutdown();

            // Then
            assertEquals(1, smallQueue.size(), "지수 백오프 후 큐는 여전히 꽉 차 있어야 합니다.");
            assertFalse(backoffTimes.isEmpty(), "백오프 시간이 기록되어야 합니다.");
            assertTrue(backoffTimes.get(0) >= 1,
                    "백오프 시간이 최소 초기 백오프 시간 이상이어야 합니다.");
        }
    }

    @Nested
    @DisplayName("pollBatch 메서드 테스트")
    class PollBatchOperationTest {

        @Test
        @DisplayName("큐에서 일괄로 항목 꺼내기")
        void testPollBatch() {
            // Data (Given)
            InMemoryQueue<Integer> queue = new InMemoryQueue<>(10);
            for (int i = 1; i <= 10; i++) {
                queue.offer(i);
            }

            // When
            List<Integer> batch = queue.pollBatch(5);

            // Then
            assertEquals(5, batch.size(), "pollBatch로 꺼낸 항목의 개수는 5개여야 합니다.");
            assertEquals(List.of(1, 2, 3, 4, 5), batch, "pollBatch로 꺼낸 항목은 [1, 2, 3, 4, 5]이어야 합니다.");
            assertEquals(5, queue.size(), "pollBatch 이후 큐에 남아있는 항목의 개수는 5개여야 합니다.");
        }

        @Test
        @DisplayName("큐에 남아있는 항목보다 많은 수를 요청할 경우")
        void testPollBatchMoreThanAvailable() {
            // Data (Given)
            InMemoryQueue<Integer> queue = new InMemoryQueue<>(10);
            for (int i = 1; i <= 8; i++) {
                queue.offer(i);
            }

            // When
            List<Integer> batch = queue.pollBatch(10);

            // Then
            assertEquals(8, batch.size(), "pollBatch로 꺼낸 항목의 개수는 8개여야 합니다.");
            assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), batch, "pollBatch로 꺼낸 항목은 [1, 2, 3, 4, 5, 6, 7, 8]이어야 합니다.");
            assertTrue(queue.isEmpty(), "pollBatch 이후 큐는 비어 있어야 합니다.");
        }

        @Test
        @DisplayName("동시성을 고려한 pollBatch 동작 테스트")
        void testPollBatchWithConcurrency() throws InterruptedException {
            // Data (Given)
            InMemoryQueue<Integer> queue = new InMemoryQueue<>(100);
            for (int i = 1; i <= 100; i++) {
                queue.offer(i);
            }

            // Context
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(5);
            List<List<Integer>> allBatches = new ArrayList<>();
            List<Exception> exceptions = new ArrayList<>();

            // Interaction (When)
            for (int i = 0; i < 5; i++) {
                executorService.submit(() -> {
                    try {
                        List<Integer> batch = queue.pollBatch(20);
                        synchronized (allBatches) {
                            allBatches.add(batch);
                        }
                    } catch (Exception e) {
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // Then
            assertEquals(5, allBatches.size(), "5개의 배치가 생성되어야 합니다.");
            assertTrue(exceptions.isEmpty(), "동시성 pollBatch 작업 중 예외가 발생하지 않아야 합니다.");
            assertTrue(queue.isEmpty(), "모든 배치 작업 후 큐는 비어 있어야 합니다.");

            // 중복 없이 모든 요소가 정확히 한 번씩 포함되어야 함
            List<Integer> allItems = new ArrayList<>();
            for (List<Integer> batch : allBatches) {
                allItems.addAll(batch);
            }
            allItems.sort(Integer::compareTo);
            assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                            21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
                            61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
                            81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100),
                    allItems, "큐에서 꺼낸 모든 항목은 [1, 2, ..., 100] 순서대로 중복 없이 포함되어야 합니다.");
        }
    }
}
