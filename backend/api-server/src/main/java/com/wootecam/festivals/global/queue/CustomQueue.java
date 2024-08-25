package com.wootecam.festivals.global.queue;

import java.util.List;

public interface CustomQueue<T> {
    void offer(T item);
    T poll();
    boolean isEmpty();
    int size();
    List<T> pollBatch(int batchSize);
    void clear();
}
