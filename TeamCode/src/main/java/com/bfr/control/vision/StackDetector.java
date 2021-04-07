package com.bfr.control.vision;

import android.media.audiofx.AcousticEchoCanceler;

import com.bfr.util.AllianceColor;
import com.bfr.util.FTCUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;
import java.util.List;

import static com.bfr.control.vision.StackDetectorConstants.*;

public class StackDetector {
    private Rect stackRect;

    private static Scalar min = new Scalar(minHue, minSat, 0);
    private static Scalar max = new Scalar(maxHue, maxSat, 255);

    // as fields to prevent memory leaks
    private Mat latestFrame = new Mat();
    private Mat stackMat = new Mat();
    private Mat blurredMat = new Mat();
    private Mat binaryMat = new Mat();
    private Mat hierarchy = new Mat();

    private List<MatOfPoint> contours = new ArrayList<>();

    private Cam cam;


    public enum FieldConfiguration {
        ZERO,
        ONE,
        FOUR
    }

    public StackDetector() {
        cam = new Cam("stack_cam");

        //todo tune
        if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
            stackRect = new Rect(1663, 38, 150, 150);
        } else {
            stackRect = new Rect(16, 46, 150, 150);
        }

        cam.start(OpenCvCameraRotation.UPRIGHT);

//        Mat temp = new Mat();
//        cam.copyFrameTo(temp);
//
//        FTCUtilities.saveImage(temp, "stack.png");
//        temp.release();
    }

    public FieldConfiguration getFieldConfiguration(){

        cam.copyFrameTo(latestFrame);

        stackMat = latestFrame.submat(stackRect);

        Imgproc.blur(stackMat, blurredMat, new Size(15,15));

        Core.inRange(blurredMat, min, max, binaryMat);

        cam.setOutputMat(binaryMat);

        contours.clear();
        Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        if(contours.size() == 0){
            return FieldConfiguration.ZERO;
        }

        double largestArea = 0.0;
        for (MatOfPoint mat : contours) {
            double area = Imgproc.contourArea(mat);

            if(area > largestArea){
                largestArea = area;
            }
        }

        //System.out.println(largestArea);

        if(largestArea <  zeroRingAreaMax) {
            return FieldConfiguration.ZERO;
        } else if (largestArea > fourRingAreaMin) {
            return FieldConfiguration.FOUR;
        } else return FieldConfiguration.ONE;
    }

    public void stop(){
        cam.stop();
    }
}
