package com.bfr.control.vision;

import com.bfr.util.FTCUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Vision system designed for Temerity in 2020-21 UG.
 * Looks for the white vision target on the backboard and deduces the location of the robot.
 * @author Alex Appleby, Team 16896
 */
public class VisionSystem2 {
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

    //intermediate mats
    private Mat backboardThresholdMat, backboardTrimmedMat, unprocessedTarget, processedTarget;

    public VisionSystem2(boolean streamMode) {
        cam = new Cam("Webcam 1", 1080, 1920, -1);

        this.streamMode = streamMode;

        cam.start(OpenCvCameraRotation.UPSIDE_DOWN);

        Mat initMat = new Mat();
        cam.copyFrameTo(initMat);

    }

    //todo when finished: run step by step through calcs and check for memory leaks.
    public void runVision() throws VisionException {
        update();
        backboardThresholdMat = backboardThresholdPipeline.processFrame(currentMat);

        CroppedMatContainer backboardContainer = cropBackboard(backboardThresholdMat);
        backboardTrimmedMat = backboardContainer.mat;

        //keep track of the offsets between different frames of reference.
        xOffset_fullToBackboard = backboardContainer.xOffset;
        yOffset_fullToBackboard = backboardContainer.yOffset;

        CroppedMatContainer targetContainer = getTarget(backboardTrimmedMat, currentMat, xOffset_fullToBackboard, yOffset_fullToBackboard);

        unprocessedTarget = targetContainer.mat;
        processedTarget = processTarget(unprocessedTarget);

        drawTargetCorners(processedTarget);

//        System.out.println("size: " + targetContainer.mat.size());

        //Imgproc.cvtColor(targetContainer.mat, targetContainer.mat, Imgproc.COLOR_GRAY2RGB);
        cam.setOutputMat(processedTarget);

        //release mats to avoid memory leak on the native heap
        backboardThresholdMat.release();
        backboardTrimmedMat.release();
        backboardContainer.mat.release();
        targetContainer.mat.release();
        unprocessedTarget.release();
        processedTarget.release();
        //FTCUtilities.saveImage(currentMat);
    }

    public void calibrate(){
        update();

        double avgHue = VisionUtil.findAvgOfRegion(currentMat, 720,345,55,90, VisionUtil.HSVChannel.HUE);
        //System.out.println("calibration hue: " + avgHue);
        backboardThresholdPipeline.setHue(avgHue);
    }

    public void saveCurrentFrame(){
        update();
        FTCUtilities.saveImage(currentMat);
    }

    private void update(){
        cam.copyFrameTo(currentMat);
    }

    /**
     * Dumps the current frame + all intermediate frames to the robot hard drive.
     * For debugging purposes.
     */
    public void dump(){
        String directory = "vision_dump/";

        Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_HSV2BGR);
        FTCUtilities.saveImage(currentMat, directory + "1_unprocessed.png");

        if (backboardThresholdMat != null) {
            if (!backboardThresholdMat.empty()) {
                Imgproc.cvtColor(backboardThresholdMat, backboardThresholdMat, Imgproc.COLOR_GRAY2BGR);
                FTCUtilities.saveImage(backboardThresholdMat, directory + "2_bb_thresh.png");
            }
        }

        if (backboardTrimmedMat != null) {
            if (!backboardTrimmedMat.empty()) {
                Imgproc.cvtColor(backboardTrimmedMat, backboardTrimmedMat, Imgproc.COLOR_GRAY2BGR);
                FTCUtilities.saveImage(backboardTrimmedMat, directory + "3_bb_trimmed.png");
            }
        }

        if (unprocessedTarget != null) {
            if (!unprocessedTarget.empty()) {
                Imgproc.cvtColor(unprocessedTarget, unprocessedTarget, Imgproc.COLOR_HSV2BGR);
                FTCUtilities.saveImage(unprocessedTarget, directory + "4_target.png");
            }
        }

        if (processedTarget != null) {
            if (!processedTarget.empty()) {
                Imgproc.cvtColor(processedTarget, processedTarget, Imgproc.COLOR_GRAY2BGR);
                FTCUtilities.saveImage(processedTarget, directory + "5_target_processed.png");
            }
        }
    }

    //******************** VISION PROCESSING MEMBERS ***************************
    //Some vision processing will be done in OpenCvPipelines, where pipeline conventions can be neatly used
    //Other vision processing isn't 'Mat in -> Mat out' style. Those steps have their own method here.

    /**
     * Take a mat with a thresholded backboard and crop it down to its bounding box.
     * Save the offset while we're at it.
     */
    static CroppedMatContainer cropBackboard(Mat input) throws VisionException {
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
    static CroppedMatContainer getTarget(Mat input, Mat original, int xOffset, int yOffset) throws VisionException {
        Mat inverted = new Mat();
        Core.bitwise_not(input, inverted);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(inverted, contours, new Mat(),  Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        MatOfPoint2f largestContour = new MatOfPoint2f(VisionUtil.findLargestContour(contours).toArray());
        Rect rect = Imgproc.boundingRect(largestContour);
        //convert largest contour to matOfPoint2f

        int padding = 7;
        if(rect.x + xOffset < padding || rect.y + yOffset < padding){
            throw new VisionException("insufficient space around target for corner detection padding");
        }

        //add padding to the corners of the Rect so that we can run corner detection and
        //have some space to work with
        VisionUtil.padRect(rect, padding);

        //save the target's frame of reference in relation to the backboard
        int backboardXOffset = rect.x;
        int backboardYOffset = rect.y;

        //convert the rectangle from the frame of reference of the backboard
        //to the frame of reference of the original image
        rect.x += xOffset;
        rect.y += yOffset;

//        System.out.println("x: " + rect.x);
//        System.out.println("y: " + rect.y);
//        System.out.println("w: " + rect.width);
//        System.out.println("h: " + rect.height);
//
        Mat coloredTarget = original.submat(rect);
        Mat copy = new Mat();
        coloredTarget.copyTo(copy);

        inverted.release();
        largestContour.release();
        coloredTarget.release();

        return new CroppedMatContainer(copy, backboardXOffset, backboardYOffset);
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

    /**
     * Extracts the saturation channel of the target mat and blurs out
     * the center region where the logo is.
     */
    static Mat processTarget(Mat input){
        Mat sat = VisionUtil.getHSVChannel(input, VisionUtil.HSVChannel.SATURATION);

        //blur the inner 2/3 of the image
        int blurWidth = (sat.width() / 3) * 2;
        int blurHeight = (sat.height() / 3) * 2;
        int xOffset = (sat.width() - blurWidth) / 2;
        int yOffset = (sat.height() - blurHeight) / 2;

        Rect rect = new Rect(xOffset, yOffset, blurWidth,blurHeight);
        Mat sub = sat.submat(rect);
        //Imgproc.GaussianBlur(sub, sub, new Size(15,15), 0);
        Imgproc.blur(sub, sub, new Size(15,15));
        //Imgproc.threshold(sub, sub, 0,5, Imgproc.THRESH_BINARY);

        return sat;
    }

    /**
     * Takes an image and finds the 4 sharpest corners.
     * Uses Shi-Tomasi method of corner detection, takes some nice parameters
     */
    static void drawTargetCorners(Mat input) throws VisionException {

        MatOfPoint corners = new MatOfPoint();

        Imgproc.goodFeaturesToTrack(input, corners, 4, 0.01, 50);

        Point[] cornerArray = corners.toArray();

        if (cornerArray.length != 4) {
            throw new VisionException("Exception finding corners: " + cornerArray.length + " corners found.");
        }

        for (Point p: cornerArray) {
            Imgproc.circle(input, p, 3, new Scalar(0), 1);
        }
    }


}
