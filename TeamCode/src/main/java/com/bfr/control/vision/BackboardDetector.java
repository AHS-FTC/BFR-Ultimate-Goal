package com.bfr.control.vision;


import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;
import java.util.List;

public class BackboardDetector {
    private Cam cam;
    private Mat currentFrame = new Mat();
    private Mat hierarchy = new Mat();

    private List<MatOfPoint> contours = new ArrayList<>();
    private List<MatOfPoint> validContours = new ArrayList<>();



    private BackboardThresholdPipeline backboardThresholdPipeline = new BackboardThresholdPipeline();

    public BackboardDetector() {
        cam = new Cam("shooter_cam", 1920, 1080, Math.toRadians(87.0));
    }

    public void start(){
        cam.start(OpenCvCameraRotation.UPSIDE_DOWN);
    }

    public void stop(){
        cam.stop();
    }

    public double getAngleToGoal() throws VisionException {
        if (!cam.isStreaming()){
            throw new VisionException("Attempted to get the angle to goal before the camera started streaming");
        }

        cam.copyFrameTo(currentFrame);

        Mat thresholded = backboardThresholdPipeline.processFrame(currentFrame);

        emptyContourList(contours);
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        if(contours.size() == 0){
            throw new VisionException("No contours found");
        }

        emptyContourList(validContours);
        validContours = validateContours(contours);

        Mat rgb = new Mat();
        Imgproc.cvtColor(thresholded, rgb, Imgproc.COLOR_GRAY2RGB);
        Imgproc.drawContours(rgb, validContours,-1, new Scalar(69, 255, 150), 3);
        cam.setOutputMat(rgb);
        rgb.release();

        System.out.println(validContours.size());

        if(validContours.size() == 0){
            throw new VisionException("No valid contours found");
        }

        if(validContours.size() > 1){
            throw new VisionException("Multiple potential goals found");
        }

        MatOfPoint goalContour = validContours.get(0);

        Rect r = Imgproc.boundingRect(goalContour);
        double x = (r.br().x + r.tl().x) / 2.0;

        thresholded.release();
        return cam.getAngleFromX(x);
    }

    /**
     * Prevents potential memory leak with contour lists
     */
    private static void emptyContourList(List<MatOfPoint> contours){
        for (MatOfPoint m : contours) {
            m.release();
        }
        contours.clear();

    }

    private static List<MatOfPoint> validateContours(List<MatOfPoint> contours){
        List<MatOfPoint> retVal = new ArrayList<>();

        //remove contours outside of valid bounding box area, aspect ratio
        for (MatOfPoint m : contours) {
            Rect r = Imgproc.boundingRect(m);

            double area = r.area();
            double aspectRatio = r.width / (double) r.height;

            boolean validArea = area > BackboardDetectorConstants.minArea && area < BackboardDetectorConstants.maxArea;
            boolean validAspectRatio = aspectRatio > BackboardDetectorConstants.minAspectRatio && aspectRatio < BackboardDetectorConstants.maxAspectRatio;

            if(validArea && validAspectRatio){
                retVal.add(m);
            }
        }
        return retVal;
    }
}
