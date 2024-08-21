package com.wootecam.festivals.global.queue.config;

import com.wootecam.festivals.global.queue.CustomQueue;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@EndpointWebExtension(endpoint = HealthEndpoint.class)
@RequiredArgsConstructor
public class CustomQueueHealthIndicator implements HealthIndicator {

    private final CustomQueue<PurchaseData> queue;

    @Override
    public Health health() {
        int size = queue.size();
        boolean healthy = size < 9000; // 큐의 90%이상이 차있다면 unhealthy로 처리

        return healthy
                ? Health.up().withDetail("queue.size", size).build()
                : Health.down().withDetail("queue.size", size).build();
    }
}
