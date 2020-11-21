package com.bfr.control.vision.objects;

import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionUtilTest;
import com.bfr.util.FTCUtilities;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CornerSetTest {

    @BeforeAll
    static void beforeAll() {
        //load the native OpenCv library
        System.load(System.getProperty("user.dir") + "/lib/libopencv_java410.so");

        //switch into test mode
        FTCUtilities.startTestMode();
    }

    @Test
    void testMake() throws VisionException{
        Target target = new Target();

        Backboard backboard = new Backboard();
        Mat in = VisionUtilTest.loadResourceAsMat("bluegoal.jpg");
        Imgproc.cvtColor(in, in, Imgproc.COLOR_BGR2HSV);

        backboard.make(in);

        target.make(backboard);

        CornerSet cornerSet = new CornerSet();
        cornerSet.make(target, backboard, in);

        backboard.dump();
        target.dump();
        cornerSet.dump();

        in.release();
    }

    @Test
    void testAssignPoints() throws VisionException {
        CornerSet cornerSet = new CornerSet();

        Corner topLeft = new Corner(null);
        topLeft.point = new Point(0, 0);
        Corner topRight = new Corner(null);
        topRight.point = new Point(10, 1);

        Corner bottomLeft = new Corner(null);
        bottomLeft.point = new Point(1, 5);
        Corner bottomRight = new Corner(null);
        bottomRight.point = new Point(11, 6);


        List<Corner> list = new ArrayList<>();
        list.add(bottomLeft);
        list.add(topRight);
        list.add(bottomRight);
        list.add(topLeft);

        cornerSet.assignCorners(list);

        assertEquals(cornerSet.corners.get(CornerSet.CornerType.TOP_LEFT), topLeft);
        assertEquals(cornerSet.corners.get(CornerSet.CornerType.TOP_RIGHT), topRight);
        assertEquals(cornerSet.corners.get(CornerSet.CornerType.BOTTOM_LEFT), bottomLeft);
        assertEquals(cornerSet.corners.get(CornerSet.CornerType.BOTTOM_RIGHT), bottomRight);
    }
}