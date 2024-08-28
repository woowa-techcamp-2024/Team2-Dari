package com.wootecam.festivals.domain.wait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("대기열 입장 가능 순서 갱신 시")
    class Describe_updateByWaitOrder {
        @Test
        @DisplayName("현재 대기열 입장 가능 범위보다 현재 대기 번호가 크다면 대기열 입장 범위 오프셋을 청크만큼 증가시킨다")
        void testUpdateByWaitOrder_UpdateSuccess() {
            // given
            passOrder.set(1L, 5L);

            // when
            Long updatedOrder = passOrder.updateByWaitOrder(1L, 11L, 5L);

            // then
            assertAll(() -> assertThat(updatedOrder).isEqualTo(10L));
        }

        @Test
        @DisplayName("현재 대기열 입장 가능 범위보다 현재 대기 그룹 크기가 작거나 같다면 대기열 입장 범위를 변경하지 않는다")
        void testUpdateByWaitOrder_NoUpdateNeeded() {
            // given
            passOrder.set(1L, 7L);

            // when
            Long updatedOrder = passOrder.updateByWaitOrder(1L, 7L, 5L);

            // then
            assertAll(() -> assertThat(updatedOrder).isEqualTo(7L));
        }

        @Test
        @DisplayName("대기열 순서가 초기화되지 않았다면 예외를 발생시킨다")
        void testUpdateByWaitOrder_ThrowsExceptionWhenNotInitialized() {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                passOrder.updateByWaitOrder(2L, 5L, 3L);
            });
        }
    }
}
