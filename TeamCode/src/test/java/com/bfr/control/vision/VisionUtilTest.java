package com.bfr.control.vision;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.junit.jupiter.api.Assertions.*;

public class VisionUtilTest {
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

    @Test
    void testPadRect() {
        Rect rect = new Rect(1,1,5,10);
        VisionUtil.padRect(rect, 1);

        assertEquals(rect.x, 0);
        assertEquals(rect.y, 0);
        assertEquals(rect.height, 12);
        assertEquals(rect.width, 7);
    }

    /**
     * Loads file in resources
     */
    public static Mat loadResourceAsMat(String filename){
        String resPath = VisionTests.class.getClassLoader().getResource(filename).getPath();

        return Imgcodecs.imread(resPath);
    }

    public static Mat loadResourceAsMatBinary(String filename){
        Mat m = loadResourceAsMat(filename);
        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
        Mat retVal = new Mat();

        Imgproc.threshold(m , retVal, 100, 255, Imgproc.THRESH_BINARY);

        m.release();
        return retVal;
    }
}