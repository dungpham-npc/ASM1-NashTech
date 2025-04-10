package com.dungpham.asm1;

import com.dungpham.asm1.service.impl.TestService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestServiceTest {
    private final TestService testService = new TestService();

    @Test
    void testAddition() {
        // given
        int a = 6;
        int b = 7;

        // when
        int result = testService.test(a, b);

        // then
        assertEquals(12, result);
    }
}
