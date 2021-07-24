package com.bfr.control.vision;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CamTest {

    @Test
    void testFOVS() {
        //assertEquals(Cam.FOCAL_LENGTH_PX, Cam.FOCAL_LENGTH_PX_2, 0.01);
    }

    @Test
    void testAngleXMiddle() {
        //double angle = Cam.getAngleFromX(Cam.middleX);
        //assertEquals(0, angle);
    }

    @Test
    void testAngleXLeft() {
        //double angle = Cam.getAngleFromX(0);
        //assertEquals(Math.toDegrees(Cam.fovH) / -2.0, angle);
    }

    @Test
    void testAngleXRight() {
        //double angle = Cam.getAngleFromX(Cam.width);
        //assertEquals(Math.toDegrees(Cam.fovH) / 2.0, angle);
    }
}