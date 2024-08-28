package com.wootecam.festivals.domain.wait.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.wootecam.festivals.utils.SpringBootTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

class PassOrderRedisRepositoryTest extends SpringBootTestConfig {

    private final Long ticketId = 1L;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private PassOrderRedisRepository passOrderRedisRepository;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("increase 메소드는 대기열 순번을 증가시키고 올바른 값을 반환한다")
    void increase_shouldIncrementPassOrderAndReturnCorrectValue() {
        // Given
        Long passOrderChunkSize = 5L;
        Long curWaitOrder = 10L;

        // When
        Long newPassOrder = passOrderRedisRepository.increase(ticketId, passOrderChunkSize, curWaitOrder);

        // Then
        assertThat(newPassOrder).isEqualTo(5L);

        newPassOrder = passOrderRedisRepository.increase(ticketId, passOrderChunkSize, curWaitOrder);
        assertThat(newPassOrder).isEqualTo(10L);
    }

    @Test
    @DisplayName("increase 메소드는 현재 대기열 순번보다 큰 값으로 증가하지 않는다")
    void increase_shouldNotExceedCurrentWaitOrder() {
        // Given
        Long passOrderChunkSize = 5L;
        Long curWaitOrder = 8L;

        // When
        Long newPassOrder = passOrderRedisRepository.increase(ticketId, passOrderChunkSize, curWaitOrder);

        // Then
        assertThat(newPassOrder).isEqualTo(5L);

        newPassOrder = passOrderRedisRepository.increase(ticketId, passOrderChunkSize, curWaitOrder);
        assertThat(newPassOrder).isEqualTo(5L);
    }

    @Test
    @DisplayName("increase 메소드는 새로운 순번이 현재 대기열 순번과 같으면 그 값을 설정한다")
    void increase_shouldSetNewPassOrderWhenEqualToCurWaitOrder() {
        // Given
        Long passOrderChunkSize = 5L;
        Long curWaitOrder = 10L;

        // When
        Long newPassOrder = passOrderRedisRepository.increase(ticketId, passOrderChunkSize, curWaitOrder);
        assertThat(newPassOrder).isEqualTo(5L);

        newPassOrder = passOrderRedisRepository.increase(ticketId, passOrderChunkSize, curWaitOrder);
        assertThat(newPassOrder).isEqualTo(10L);
    }
}
