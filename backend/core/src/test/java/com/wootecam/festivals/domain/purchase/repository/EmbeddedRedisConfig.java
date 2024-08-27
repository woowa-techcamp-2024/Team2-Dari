package com.wootecam.festivals.domain.purchase.repository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

    private String REDIS_PORT = "6379";

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        redisServer = new RedisServer(Integer.parseInt(REDIS_PORT));
        try {
            redisServer.start();
        } catch (Exception e) {
            System.out.println("Redis server start failed");
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            try {
                redisServer.stop();
            } catch (Exception e) {
                System.out.println("Redis server stop failed");
                e.printStackTrace();
            }
        }
    }
}
