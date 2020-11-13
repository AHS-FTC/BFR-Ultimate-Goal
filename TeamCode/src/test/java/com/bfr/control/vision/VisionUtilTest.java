package com.bfr.control.vision;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.junit.jupiter.api.Assertions.*;

class VisionUtilTest {
    @BeforeAll
    static void beforeAll() {
        System.load(System.getProperty("user.dir") + "/lib/libopencv_java410.so");
    }

    @Test
    void testRegionAverage() {
        Mat in = VisionTests.loadResourceAsMat("pinkcorner.png");

        Mat hsv = new Mat();
        Imgproc.cvtColor(in, hsv, Imgproc.COLOR_BGR2HSV);

        assertEquals(161, VisionUtil.findAvgOfRegion(hsv, 0, 400, 100, 100, VisionUtil.HSVChannel.HUE));
        assertEquals(133, VisionUtil.findAvgOfRegion(hsv, 0, 400, 100, 100, VisionUtil.HSVChannel.SATURATION));
        assertEquals(243, VisionUtil.findAvgOfRegion(hsv, 0, 400, 100, 100, VisionUtil.HSVChannel.VALUE));
    }
}