package com.bfr.control.teleop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalControlCurveTest {

    @Test
    void testPeak() {
        NormalControlCurve curve = new NormalControlCurve(3.0, 0.1,3, false);

        assertEquals(1, curve.eval(3.0));

        NormalControlCurve curve2 = new NormalControlCurve(-3.0, -0.1,4, true);

        assertEquals(-1, curve2.eval(-3.0), 0.01);
    }

    @Test
    void testEdges() {
        NormalControlCurve curve = new NormalControlCurve(5.0, 0.2,3, false);

        assertEquals(0.2, curve.eval(Double.POSITIVE_INFINITY));
        assertEquals(0.2, curve.eval(Double.NEGATIVE_INFINITY));

        NormalControlCurve curve2 = new NormalControlCurve(-5.0, -0.2,3, true);

        assertEquals(-0.2, curve2.eval(Double.POSITIVE_INFINITY));
        assertEquals(-0.2, curve2.eval(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testDecreasing() {
        NormalControlCurve curve2 = new NormalControlCurve(1, 0.15, 0.2, false);

        assertEquals(1, curve2.eval(1));

        NormalControlCurve curve = new NormalControlCurve(-1, -0.15, 0.2, true);

        assertEquals(-1, curve.eval(-1));
    }




}