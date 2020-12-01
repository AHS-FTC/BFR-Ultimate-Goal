package com.bfr.control.pidf;

import com.bfr.util.FTCUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PIDFControllerTest {
    @BeforeEach
    void setUp() {
        FTCUtilities.startTestMode();
    }

    @Test
    void testProportional() {
        PIDFController controller = new PIDFController(2,0,0,10,0);

        assertEquals(0, controller.getOutput(10));

        //error of 1 scaled by 2
        assertEquals(2.0, controller.getOutput(9), 0.0001);
    }

    @Test
    void testIntegral() {
        PIDFController controller = new PIDFController(0,1,0,10,0);

        //delta time progresses by 1 ms each call
        assertEquals(5, controller.getOutput(5));
        assertEquals(10, controller.getOutput(5));
        assertEquals(15, controller.getOutput(5));
    }

    @Test
    void testDerivative() {
        //note initial value = 0
        PIDFController controller = new PIDFController(0,0,1,10,0);

        //derivative = (0 - 5) / delta time * kD, should be negative
        assertEquals(-5, controller.getOutput(5));
        assertEquals(0, controller.getOutput(5));
        assertEquals(-5, controller.getOutput(10));
    }
}