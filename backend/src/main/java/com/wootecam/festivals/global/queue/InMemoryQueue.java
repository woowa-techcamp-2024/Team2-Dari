package com.wootecam.festivals.global.queue;

import com.wootecam.festivals.global.queue.exception.QueueFullException;
import com.wootecam.festivals.global.queue.exception.QueueOperationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * LinkedBlockingQueue를 기반으로 한 인메모리 큐 구현
 * 스레드 안전성과 블로킹 연산을 제공하여 동시성 처리에 적합합니다.
 */
public class InMemoryQueue<T> implements CustomQueue<T> {

    // LinkedBlockingQueue: 내부적으로 락을 사용하여 스레드 안전성을 보장하는 큐
    private final LinkedBlockingQueue<T> queue;
    private final int capacity;

    /**
     * 지정된 용량의 InMemoryQueue를 생성합니다.
     * @param capacity 큐의 최대 용량
     */
    public InMemoryQueue(int capacity) {
        this.capacity = capacity;
        // LinkedBlockingQueue 생성 시 최대 용량을 지정하여 메모리 사용을 제한합니다.
        queue = new LinkedBlockingQueue<>(capacity);
    }

    /**
     * 큐에 아이템을 추가합니다.
     * 큐가 가득 찼거나 5초 동안 추가할 수 없는 경우 예외를 발생시킵니다.
     * @param item 추가할 아이템
     * @throws QueueFullException 큐가 가득 찼거나 지정된 시간 내에 추가할 수 없는 경우
     * @throws QueueOperationException 인터럽트 발생 시
     */
    @Override
    public void offer(T item) {
        try {
            // offer 메서드로 아이템 추가 시도. 5초 동안 기다립니다.
            // 큐가 가득 찼거나 5초 내에 추가할 수 없으면 false 반환
            if (!queue.offer(item, 5, TimeUnit.SECONDS)) {
                throw new QueueFullException("큐가 가득 찼습니다.");
            }
        } catch (InterruptedException e) {
            // 인터럽트 발생 시 현재 스레드의 인터럽트 상태를 다시 설정하고 예외 발생
            Thread.currentThread().interrupt();
            throw new QueueOperationException("아이템을 큐에 추가하던 중 인터럽트가 발생했습니다.");
        }
    }

    /**
     * 큐에서 아이템을 꺼내고 제거합니다.
     * 큐가 비어있으면 null을 반환합니다.
     * @return 큐에서 꺼낸 아이템, 큐가 비어있다면 null
     */
    @Override
    public T poll() {
        // poll 메서드는 큐가 비어있을 경우 null을 반환합니다.
        return queue.poll();
    }

    /**
     * 큐가 비어있는지 확인합니다.
     * @return 큐가 비어있으면 true, 그렇지 않으면 false
     */
    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * 현재 큐에 저장된 아이템의 개수를 반환합니다.
     * @return 큐에 저장된 아이템의 개수
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * 큐에서 여러 아이템을 한 번에 꺼냅니다.
     * @param batchSize 꺼낼 아이템의 최대 개수
     * @return 꺼낸 아이템들의 리스트
     */
    @Override
    public List<T> pollBatch(int batchSize) {
        List<T> batch = new ArrayList<>(batchSize);
        // drainTo 메서드를 사용하여 큐에서 batchSize만큼의 아이템을 한 번에 꺼내 리스트에 추가
        // 이 작업은 원자적으로 수행되어 스레드 안전성을 보장합니다.
        queue.drainTo(batch, batchSize);
        return batch;
    }
}