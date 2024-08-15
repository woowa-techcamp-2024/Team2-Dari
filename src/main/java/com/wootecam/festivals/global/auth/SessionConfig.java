package com.wootecam.festivals.global.auth;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

@Configuration
@EnableSpringHttpSession
public class SessionConfig {
    @Bean
    public SessionRepository sessionRepository() {
        return new MapSessionRepository(new ConcurrentHashMap<>());
    }
}
