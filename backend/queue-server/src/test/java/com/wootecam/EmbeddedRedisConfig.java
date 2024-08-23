package com.wootecam;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port}")
    private String redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        redisServer = new RedisServer(Integer.parseInt(redisPort));
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
