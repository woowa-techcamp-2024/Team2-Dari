package com.wootecam.festivals.global.config;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfig {

    @Bean
    public CustomMapSessionRepository sessionRepository() {
        return new CustomMapSessionRepository(new ConcurrentHashMap<>());
    }
}
