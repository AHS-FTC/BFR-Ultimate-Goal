package com.bfr.control.vision;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class VisionTests {
    @BeforeAll
    static void beforeAll() {
        System.load(System.getProperty("user.dir") + "/lib/libopencv_java410.so");
    }

    @Test
    @Disabled
    void testThresholdBlueBackboard() {
        BackboardThresholdPipeline pipeline = new BackboardThresholdPipeline();
        pipeline.setHue(110);

        Mat in = loadResourceAsMat("bluegoal.jpg");

        Mat out = pipeline.processFrame(in);

        Imgcodecs.imwrite("/home/appleby/Desktop/out.png", out);
    }

    @Test
    //@Disabled
    void testCropBackboard() {
        BackboardCropPipeline pipeline = new BackboardCropPipeline();

        Mat in = loadResourceAsMat("threshgoal.png");
        //Imgproc.cvtColor(in, in, Imgproc.COLOR);

        Mat out = pipeline.processFrame(in);

        Imgcodecs.imwrite("/home/appleby/Desktop/out.png", out);
    }

    @Test
        //@Disabled
    void testCornerFinder() {
        Mat in = loadResourceAsMatBinary("threshcroppedgoal.png");

        VisionUtil.findViewpointCorners(in);

        Imgcodecs.imwrite("/home/appleby/Desktop/out.png", in);
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
