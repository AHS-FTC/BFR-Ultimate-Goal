package com.bfr.control.vision.objects;

import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionSystem2;
import com.bfr.control.vision.VisionUtil;
import com.bfr.util.FTCUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Backboard extends VisionObject {

    private Scalar min = new Scalar(100, 50, 0);
    private Scalar max = new Scalar(120, 255, 255);

    private static final double HUE_RANGE = 10;
    private static final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
    private static final double heightRatio = 2.0 / 3.0;

    private final Mat blurred = new Mat();
    private final Mat thresh = new Mat();
    private final Mat eroded = new Mat();
    private final Mat dilated = new Mat();

    private final List<MatOfPoint> contours = new ArrayList<>();
    private final Mat hierarchy = new Mat();

    //protected for use in other VisionObjects and tests
    public Mat binaryCropped = new Mat(), colorCropped = new Mat();
    double xOffset, yOffset;

    //the middle x pixel of the backboard
    private double middleX = 0.0;

    public Backboard(){}

    /**
     * @param fullImage Image directly from camera in HSV form
     */
    public void make(Mat fullImage) throws VisionException {
        Imgproc.GaussianBlur(fullImage, blurred, new Size(5,5), 0);
        Core.inRange(blurred, min, max, thresh);

        Imgproc.erode(thresh, eroded, kernel);
        Imgproc.dilate(eroded, dilated, kernel);

        //Find the bounding box of the largest contour.
        Imgproc.findContours(dilated, contours, hierarchy,  Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint largestContour = VisionUtil.findLargestContour(contours);

        Rect boundingRect = Imgproc.boundingRect(largestContour);

        //trim the bounding rect to exclude the bottom portion (high goal)
        double newHeight = boundingRect.height * heightRatio;
        boundingRect.height = (int) newHeight;

        dilated.submat(boundingRect).copyTo(binaryCropped);
        fullImage.submat(boundingRect).copyTo(colorCropped);

        xOffset = boundingRect.x;
        yOffset = boundingRect.y;

        middleX = (boundingRect.tl().x + boundingRect.br().x) / 2.0;

        //empty contours to prevent memory leak
//        for (MatOfPoint c : contours) {
//            c.release();
//        }
//        largestContour.release();
    }

    /**
     * Tune the accepted hue range for backboard thresholding.
     */
    public void setHue(double middleHueVal) {
        min.set(new double[] {middleHueVal - HUE_RANGE, 50, 0});
        max.set(new double[] {middleHueVal + HUE_RANGE, 255, 255});
    }

    @Override
    public void release() {
        blurred.release();
        thresh.release();
        eroded.release();
        dilated.release();

        hierarchy.release();
        binaryCropped.release();
        colorCropped.release();
    }

    public double getMiddleX() {
        return middleX;
    }

    @Override
    public void dump() {
        String directory = "vision_dump/backboard/";

        if(!blurred.empty()){
            FTCUtilities.saveImage(blurred, directory + "1_blurred.jpg", Imgproc.COLOR_HSV2BGR);
        }

        if(!thresh.empty()){
            FTCUtilities.saveImage(thresh, directory + "2_thresh.jpg", Imgproc.COLOR_GRAY2BGR);
        }

        if(!eroded.empty()){
            FTCUtilities.saveImage(eroded, directory + "3_eroded.jpg", Imgproc.COLOR_GRAY2BGR);
        }

        if(!dilated.empty()){
            FTCUtilities.saveImage(dilated, directory + "4_dilated.jpg", Imgproc.COLOR_GRAY2BGR);
        }

        if(!binaryCropped.empty()){
            FTCUtilities.saveImage(binaryCropped, directory + "5_binary_cropped.jpg", Imgproc.COLOR_GRAY2BGR);
        }

        if(!colorCropped.empty()){
            FTCUtilities.saveImage(colorCropped,  directory + "6_color_cropped.jpg", Imgproc.COLOR_HSV2BGR);
        }
    }
}
