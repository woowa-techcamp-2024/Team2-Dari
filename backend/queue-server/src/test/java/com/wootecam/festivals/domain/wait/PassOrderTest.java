package com.wootecam.festivals.domain.wait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
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
    void testGet_WithExistingKey() {
        Long result = passOrder.get(1L);
        assertEquals(0L, result);
    }

    @Test
    void testGet_WithNonExistingKey() {
        Long result = passOrder.get(2L);
        assertEquals(0L, result);  // 기본값 0L이 반환되는지 확인
    }

    @Test
    void testSet() {
        passOrder.set(2L, 10L);
        Long result = passOrder.get(2L);
        assertEquals(10L, result);
    }

    @Test
    void testUpdateByWaitOrder_UpdateSuccess() {
        passOrder.set(1L, 5L);
        Long updatedOrder = passOrder.updateByWaitOrder(1L, 7L);

        assertEquals(6L, updatedOrder);
        assertEquals(6L, passOrder.get(1L));
    }

    @Test
    void testUpdateByWaitOrder_NoUpdateNeeded() {
        passOrder.set(1L, 7L);
        Long updatedOrder = passOrder.updateByWaitOrder(1L, 7L);

        assertEquals(7L, updatedOrder);
        assertEquals(7L, passOrder.get(1L));
    }

    @Test
    void testUpdateByWaitOrder_ThrowsExceptionWhenNotInitialized() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            passOrder.updateByWaitOrder(2L, 5L);
        });

        assertEquals("대기열 순서가 초기화되지 않았습니다.", exception.getMessage());
    }
}
