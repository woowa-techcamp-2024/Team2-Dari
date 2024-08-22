package com.wootecam.global.auth;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

@Configuration
@EnableSpringHttpSession
public class SessionConfig {

    @Bean
    public CustomMapSessionRepository sessionRepository() {
        return new CustomMapSessionRepository(new ConcurrentHashMap<>());
    }
}
