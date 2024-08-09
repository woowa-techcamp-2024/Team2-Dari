package com.wootecam.festivals.githubaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GithubActionTest {

    @Test
    void testAddition() {
        int result = 1 + 1;
        assertEquals(2, result);
    }

    @Test
    public void testSubtraction() {
        int result = 5 - 3;
        assertEquals(2, result);
    }

    @Test
    public void testMultiplication() {
        int result = 2 * 3;
        assertEquals(6, result);
    }

    @Test
    public void testDivision() {
        int result = 6 / 2;
        assertEquals(3, result);
    }

    @Test
    public void testString() {
        String str = "Hello";
        assertTrue(str.contains("ell"));
        assertFalse(str.isEmpty());
    }
}
