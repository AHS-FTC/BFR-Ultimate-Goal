package com.bfr.util.math;

import org.junit.jupiter.api.Test;

import static com.bfr.util.math.FTCMath.*;

import static org.junit.jupiter.api.Assertions.*;

class FTCMathTest {

    @Test
    void testNearestToZero() {
        assertEquals(nearestToZero(1, 0), 0);

        assertEquals(nearestToZero(1, 1), 1);

        assertEquals(nearestToZero(10, -3), -3);

        assertEquals(nearestToZero(-3, 10), -3);
    }
}