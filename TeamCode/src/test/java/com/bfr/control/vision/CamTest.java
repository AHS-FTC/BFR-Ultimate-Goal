package com.bfr.control.vision;

import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;

import static org.junit.jupiter.api.Assertions.*;

class CamTest {

    @Test
    void testFOVS() {
        assertEquals(Cam.FOCAL_LENGTH_PX, Cam.FOCAL_LENGTH_PX_2, 0.01);
    }

    @Test
    void testAngleXMiddle() {
        double angle = Cam.getAngleFromX(Cam.MIDDLE_X);
        assertEquals(0, angle);
    }

    @Test
    void testAngleXLeft() {
        double angle = Cam.getAngleFromX(0);
        assertEquals(Math.toDegrees(Cam.FOV_H) / -2.0, angle);
    }

    @Test
    void testAngleXRight() {
        double angle = Cam.getAngleFromX(Cam.RES_WIDTH);
        assertEquals(Math.toDegrees(Cam.FOV_H) / 2.0, angle);
    }
}