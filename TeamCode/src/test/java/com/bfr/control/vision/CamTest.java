package com.bfr.control.vision;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CamTest {

    @Test
    void testFOVS() {
        assertEquals(Cam.FOCAL_LENGTH_PX, Cam.FOCAL_LENGTH_PX_2, 0.01);
    }
}