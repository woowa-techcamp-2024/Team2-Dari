package com.wootecam;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SimpleTestTest {
    @Test
    void add() {
        SimpleTest simpleTest = new SimpleTest();
        assertEquals(3, simpleTest.add(1, 2));
    }
}