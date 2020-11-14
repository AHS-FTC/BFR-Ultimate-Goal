package com.bfr.control.vision;

import com.bfr.util.FTCUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Vision system designed for Temerity in 2020-21 UG.
 * Looks for the white vision target on the backboard and deduces the location of the robot.
 * @author Alex Appleby, Team 16896
 */
public class VisionSystem {
    private Cam cam;
    private boolean streamMode;

    private Mat currentMat = new Mat();

    private BackboardThresholdPipeline backboardThresholdPipeline = new BackboardThresholdPipeline();

    /**
     * How much of the backboard bounding box should we keep?
     * We'd like to crop out most of the high goal from the image.
     * This makes detecting the vision target a bit easier.
     */
    private static double heightRatio = 2.0 / 3.0;

    //breaking java conventions for readability
    //these bad boys keep the offsets between different frames of reference
    //ie. full image vs backboard only, etc.
    private int xOffset_fullToBackboard = 0;
    private int yOffset_fullToBackboard = 0;

    public VisionSystem(boolean streamMode) {
        cam = new Cam("Webcam 1");

        this.streamMode = streamMode;

        cam.start();

        Mat initMat = new Mat();
        cam.copyFrameTo(initMat);

    }

    //todo when finished: run step by step through calcs and check for memory leaks.
    public void runVision(){
        update();
        Mat backboardThresholdMat = backboardThresholdPipeline.processFrame(currentMat);

        CroppedMatContainer croppedMatContainer = cropBackboard(backboardThresholdMat);

        //keep track of the offsets between different frames of reference.
        xOffset_fullToBackboard = croppedMatContainer.xOffset;
        yOffset_fullToBackboard = croppedMatContainer.yOffset;

        cam.setOutputMat(backboardThresholdMat);

        backboardThresholdMat.release();
        croppedMatContainer.mat.release();
        //FTCUtilities.saveImage(currentMat);
    }

    public void calibrate(){
        update();

        double avgHue = VisionUtil.findAvgOfRegion(currentMat, 650,300,75,100, VisionUtil.HSVChannel.HUE);
        backboardThresholdPipeline.setHue(avgHue);
    }

    public void saveCurrentFrame(){
        update();
        FTCUtilities.saveImage(currentMat);
    }

    private void update(){
        cam.copyFrameTo(currentMat);
    }

    //******************** VISION PROCESSING MEMBERS ***************************
    //Some vision processing will be done in OpenCvPipelines, where pipeline conventions can be neatly used
    //Other vision processing isn't 'Mat in -> Mat out' style. Those steps have their own method here.

    /**
     * Take a mat with a thresholded backboard and crop it down to its bounding box.
     * Save the offset while we're at it.
     */
    static CroppedMatContainer cropBackboard(Mat input) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(input, contours, hierarchy,  Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint largestContour = VisionUtil.findLargestContour(contours);

        Rect boundingRect = Imgproc.boundingRect(largestContour);
        double newHeight = boundingRect.height * heightRatio;
        boundingRect.height = (int) newHeight;

        Mat out = input.submat(boundingRect);

        largestContour.release();
        return new CroppedMatContainer(out, boundingRect.x, boundingRect.y);
    }

    /**
     * Take a binary cropped backboard image and crops the original down to the target plus some padding
     * @param original the original full-color mat
     * @param input binary mat where white is target
     * @return the full color mat
     */
    static CroppedMatContainer getTargetRect(Mat input, Mat original, int xOffset, int yOffset){
        Mat inverted = new Mat();
        Core.bitwise_not(input, inverted);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(inverted, contours, new Mat(),  Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        MatOfPoint2f largestContour = new MatOfPoint2f(VisionUtil.findLargestContour(contours).toArray());
        Rect rect = Imgproc.boundingRect(largestContour);
        //convert largest contour to matOfPoint2f

        //add padding to the corners of the Rect so that we can run corner detection and
        //have some space to work with
        VisionUtil.padRect(rect, 7);

        //save the target's frame of reference in relation to the backboard
        int backboardXOffset = rect.x;
        int backboardYOffset = rect.y;

        //convert the rectangle from the frame of reference of the backboard
        //to the frame of reference of the original image
        rect.x += xOffset;
        rect.y += yOffset;

        Mat coloredTarget = original.submat(rect);

        inverted.release();
        largestContour.release();

        return new CroppedMatContainer(coloredTarget, backboardXOffset, backboardYOffset);
    }

    /**
     * Comes out of a method that crops a mat so we can keep track offsets and test.
     * See above.
     * Honestly just me trying to avoid mocking out hardware.
     */
    static class CroppedMatContainer {
        public final Mat mat;
        public final int xOffset, yOffset;

        public CroppedMatContainer(Mat mat, int xOffset, int yOffset) {
            this.mat = mat;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }


}
