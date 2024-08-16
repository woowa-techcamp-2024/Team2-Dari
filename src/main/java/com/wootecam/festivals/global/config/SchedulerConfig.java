package com.wootecam.festivals.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // 쓰레드 풀 크기 설정
        scheduler.setThreadNamePrefix("FestivalScheduler-");
        scheduler.initialize();

        return scheduler;
    }
}
