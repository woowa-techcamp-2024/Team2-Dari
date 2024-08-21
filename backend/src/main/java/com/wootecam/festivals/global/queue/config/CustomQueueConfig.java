package com.wootecam.festivals.global.queue.config;

import com.wootecam.festivals.global.queue.CustomQueue;
import com.wootecam.festivals.global.queue.InMemoryQueue;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomQueueConfig {

    private final int QUEUE_SIZE = 1000;

    @Bean
    public CustomQueue<PurchaseData> customQueue() {
        return new InMemoryQueue<>(1000);
    }
}
