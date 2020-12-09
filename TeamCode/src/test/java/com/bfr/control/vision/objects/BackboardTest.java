package com.bfr.control.vision.objects;

import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionTests;
import com.bfr.control.vision.VisionUtil;
import com.bfr.control.vision.VisionUtilTest;
import com.bfr.util.FTCUtilities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.junit.jupiter.api.Assertions.*;

class BackboardTest {

    @BeforeAll
    static void beforeAll() {
        //load the native OpenCv library
        System.load(System.getProperty("user.dir") + "/lib/libopencv_java410.so");

        //switch into test mode
        FTCUtilities.startTestMode();
    }

    @Test
    void testMake() throws VisionException {
        Backboard backboard = new Backboard();
        Mat in = VisionUtilTest.loadResourceAsMat("bluegoal.png");

        Imgproc.cvtColor(in, in, Imgproc.COLOR_BGR2HSV);
        backboard.make(in);

        backboard.dump();

        in.release();
    }
}