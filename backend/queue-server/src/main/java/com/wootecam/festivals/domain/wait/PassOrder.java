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

    public void clear() {
        passOrderMap.clear();
    }
}
