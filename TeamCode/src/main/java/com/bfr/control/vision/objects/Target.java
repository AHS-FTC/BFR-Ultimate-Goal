package com.bfr.control.vision.objects;

import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionUtil;
import com.bfr.util.FTCUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * A vision object of the 'target' logo on the goal.
 */
public class Target extends VisionObject {

    private Backboard backboard;
    private Mat inverted = new Mat();

    private List<MatOfPoint> contours = new ArrayList<>();
    private MatOfPoint2f largestContour;
    private MatOfPoint2f approximation = new MatOfPoint2f();

    //protected to be visible in CornerSet
    List<Point> approxCorners;

    public void make(Backboard backboard) throws VisionException {
        this.backboard = backboard;

        Core.bitwise_not(backboard.binaryCropped, inverted);

        Imgproc.findContours(inverted, contours, new Mat(),  Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        largestContour = new MatOfPoint2f(VisionUtil.findLargestContour(contours).toArray());

        double epsilon = 0.1*Imgproc.arcLength(largestContour, true);
        Imgproc.approxPolyDP(largestContour, approximation, epsilon, true);

        approxCorners = approximation.toList();
    }

    @Override
    public void release() {
        inverted.release();

        for (MatOfPoint m : contours) {
            m.release();
        }

        if(largestContour != null) {
            largestContour.release();
        }

        approximation.release();

    }

    @Override
    public void dump() {
        String directory = "vision_dump/target/";

        if(!inverted.empty()){
            FTCUtilities.saveImage(inverted, directory + "1_inverted.jpg", Imgproc.COLOR_GRAY2BGR);
        }

        //draw contours
        Mat contourOut = new Mat();
        backboard.colorCropped.copyTo(contourOut);
        Imgproc.drawContours(contourOut, contours, -1, new Scalar(55, 255, 255), 1);
        FTCUtilities.saveImage(contourOut, directory + "2_contours.jpg", Imgproc.COLOR_HSV2BGR);

        //draw points
        Mat pointOut = new Mat();
        backboard.colorCropped.copyTo(pointOut);
        for (Point p : approxCorners) {
            Imgproc.circle(pointOut, p, 1, new Scalar(55, 255, 200), 1);
        }
        FTCUtilities.saveImage(pointOut, directory + "3_approx_corners.jpg", Imgproc.COLOR_HSV2BGR);

        contourOut.release();
        pointOut.release();
    }
}
