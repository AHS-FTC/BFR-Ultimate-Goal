package com.bfr.control.vision;

import com.bfr.hardware.Robot;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class VisionUtilTest {

    @BeforeAll
    static void beforeAll() {
        System.load(System.getProperty("user.dir") + "/lib/libopencv_java410.so");
    }

    @Test
    void testRegionAverage() {
        String resPath = getClass().getClassLoader().getResource("pinkcorner.png").getPath();

        Mat m = Imgcodecs.imread(resPath);
        Mat hsv = new Mat();
        Imgproc.cvtColor(m, hsv, Imgproc.COLOR_BGR2HSV);

        assertEquals(161, VisionUtil.findAvgOfRegion(hsv, 0, 400, 100, 100, VisionUtil.HSVChannel.HUE));
        assertEquals(133, VisionUtil.findAvgOfRegion(hsv, 0, 400, 100, 100, VisionUtil.HSVChannel.SATURATION));
        assertEquals(243, VisionUtil.findAvgOfRegion(hsv, 0, 400, 100, 100, VisionUtil.HSVChannel.VALUE));
    }
}