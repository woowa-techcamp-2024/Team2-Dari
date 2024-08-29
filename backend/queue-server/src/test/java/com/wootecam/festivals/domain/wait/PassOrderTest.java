package com.wootecam.festivals.domain.wait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PassOrderTest {

    @Autowired
    private PassOrder passOrder;

    @BeforeEach
    void setUp() {
        passOrder.clear();
    }

    @Test
    @DisplayName("현재 대기열 입장 순서를 반환한다")
    void testGet_WithExistingKey() {
        // given
        passOrder.set(1L, 0L);

        // when
        Long result = passOrder.get(1L);

        // then
        assertAll(() -> assertThat(result).isEqualTo(0L));
    }

    @Test
    @DisplayName("존재하지 않는 대기열 입장 순서를 조회할 때 기본값인 0을 반환한다")
    void testGet_WithNonExistingKey() {
        // when
        Long result = passOrder.get(1L);

        // then
        assertAll(() -> assertThat(result).isEqualTo(0L));
    }

    @Test
    @DisplayName("대기열 입장 순서를 설정한다")
    void testSet() {
        // when
        passOrder.set(2L, 10L);

        // then
        Long result = passOrder.get(2L);
        assertAll(() -> assertThat(result).isEqualTo(10L));
    }
}
