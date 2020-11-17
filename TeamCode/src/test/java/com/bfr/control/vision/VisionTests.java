package com.bfr.control.vision;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * All of these vision tests aren't really conventional tests and thus should generally be disabled.
 * They're to qualitatively test and experiment with vision.
 * Instead of pass/fail, they output a mat to a file on your computer.
 *
 * If you wanna run these, you'll have to adjust the output filepath in saveMat();
 */
public class VisionTests {
    @BeforeAll
    static void beforeAll() {
        //load the native OpenCv library
        System.load(System.getProperty("user.dir") + "/lib/libopencv_java410.so");
    }

    @Test
    //@Disabled
    void testThresholdBlueBackboard() {
        BackboardThresholdPipeline pipeline = new BackboardThresholdPipeline();
        pipeline.setHue(110);

        Mat in = loadResourceAsMat("bluegoal.jpg");

        Mat out = pipeline.processFrame(in);

        saveMat(out);
    }

    @Test
    //@Disabled
    void testCropBackboard() throws VisionException{
        Mat in = loadResourceAsMatBinary("threshgoal.png");
        Mat out = VisionSystem.cropBackboard(in).mat;

        saveMat(out);
    }

    @Test
        //@Disabled
    void testRegionalBlur() {
        Mat in = loadResourceAsMat("colortarget.png");

        Mat out = VisionSystem.processTarget(in);

        saveMat(out);
    }


    @Test
        //@Disabled
    void testCornerFinder() throws VisionException{
        Mat in = loadResourceAsMat("processedtarget.png");
        Imgproc.cvtColor(in, in, Imgproc.COLOR_BGR2GRAY);

        VisionSystem.drawTargetCorners(in);

        saveMat(in);
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

    private static void saveMat(Mat out) {
        Imgcodecs.imwrite("/home/appleby/Desktop/out.png", out);
    }

}
