package com.wootecam.festivals.domain.wait;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PassOrder {

    private final Map<Long, Long> passOrderMap;

    public PassOrder() {
        this.passOrderMap = new ConcurrentHashMap<>();
        this.passOrderMap.put(1L, 0L); // todo: 스케줄링 기반 초기화 로직으로 수정 예정
    }

    public Long get(Long key) {
        return passOrderMap.getOrDefault(key, 0L);
    }

    public Long set(Long key, Long value) {
        return passOrderMap.put(key, value);
    }

    public Long updateByWaitOrder(Long key, Long currentWaitOrder, Long passChunkSize) {
        if (!passOrderMap.containsKey(key)) {
            log.warn("대기열 순서가 초기화되지 않았습니다. - {}", key);
            throw new IllegalStateException("대기열 순서가 초기화되지 않았습니다.");
        }

        passOrderMap.computeIfPresent(key, (k, waitOrder) -> {
            Long newWaitOrder = waitOrder + passChunkSize;
            if (newWaitOrder < currentWaitOrder) {
                log.debug("현재 입장 순서 갱신 - key: {} value: {}", key, newWaitOrder);
                return newWaitOrder;
            }
            return waitOrder;
        });

        return passOrderMap.get(key);
    }

    public void clear() {
        passOrderMap.clear();
    }
}
