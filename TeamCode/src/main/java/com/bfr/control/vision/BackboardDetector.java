package com.bfr.control.vision;


import com.bfr.control.vision.objects.PotentialBackboard;
import com.bfr.control.vision.objects.Powershots;

import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackboardDetector {
    private Cam cam;
    private Mat currentFrame = new Mat();
    private Mat hierarchy = new Mat();

    private List<MatOfPoint> contours = new ArrayList<>();
    private List<PotentialBackboard> potentialBackboards = new ArrayList<>();
    private List<MatOfPoint> powershotContours = new ArrayList<>();


    private BackboardThresholdPipeline backboardThresholdPipeline = new BackboardThresholdPipeline();
    private PowershotThresholdPipeline powershotThresholdPipeline = new PowershotThresholdPipeline();

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
        MatOfPoint goalContour = getGoalContour();

        Rect r = Imgproc.boundingRect(goalContour);
        double x = (r.br().x + r.tl().x) / 2.0;

        goalContour.release();

        return cam.getAngleFromX(x);
    }

    public Map<Powershots.Position, Double> getAnglesToPowershots() throws VisionException{
        MatOfPoint goalContour = getGoalContour();

        Rect r = Imgproc.boundingRect(goalContour);

        goalContour.release();

        int cropHeight = (int) (r.height / 1.35);

        //crop a region near the goal where the powershots should (roughly) be
        Rect cropRegion = new Rect((int)r.br().x, (int)(r.br().y - (cropHeight * .7)), (int) (r.width * 1.35), cropHeight);

        Mat threshMat = powershotThresholdPipeline.processFrame(currentFrame);


        Mat croppedMat;
        try {
            croppedMat = threshMat.submat(cropRegion);
        } catch (CvException e){
            throw new VisionException("Powershots were out of frame");
        }

        //Imgproc.rectangle(threshMat, cropRegion, new Scalar(255), 3);
        //cam.setOutputMat(croppedMat);

        VisionUtil.emptyContourList(powershotContours);
        Imgproc.findContours(croppedMat, powershotContours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);


        Powershots powershots = new Powershots(powershotContours);

        Map<Powershots.Position, Double> anglesTo = new HashMap<>();

        //convert powershot x positions into angles and return
        for (Powershots.Position p : Powershots.Position.values()){


            double xPos = powershots.xPositionsMap.get(p);
            //draw debugging line on x pos
            Imgproc.line(croppedMat, new Point(xPos, 0), new Point(xPos, croppedMat.height() - 1), new Scalar(100), 2);


            //add back in the x value of the crop region to move from the ROI to the full frame image.
            double angleTo = cam.getAngleFromX(xPos + cropRegion.x);
            anglesTo.put(p, angleTo);
        }

        cam.setOutputMat(threshMat);

        threshMat.release();

        croppedMat.release();

        return anglesTo;
    }


    private MatOfPoint getGoalContour() throws VisionException {
        if (!cam.isStreaming()){
            throw new VisionException("Attempted to get the angle to goal before the camera started streaming");
        }

        cam.copyFrameTo(currentFrame);

        Mat thresholded = backboardThresholdPipeline.processFrame(currentFrame);

        VisionUtil.emptyContourList(contours);
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        if(contours.size() == 0){
            throw new VisionException("No contours found");
        }

        potentialBackboards = validateContours(contours);

//        Mat rgb = new Mat();
//        Imgproc.cvtColor(thresholded, rgb, Imgproc.COLOR_GRAY2RGB);
//        Imgproc.drawContours(rgb, validContours,-1, new Scalar(69, 255, 150), 3);
//        cam.setOutputMat(rgb);
//        rgb.release();

        if(potentialBackboards.size() == 0){
            throw new VisionException("No valid contours found");
        }

        if(potentialBackboards.size() > 1){
            throw new VisionException("Multiple potential goals found");
        }

        thresholded.release();

        return potentialBackboards.get(0).contour;
    }

    public static List<PotentialBackboard> validateContours(List<MatOfPoint> contours){
        List<PotentialBackboard> retVal = new ArrayList<>();

        //remove contours outside of valid bounding box area, aspect ratio
        for (int i = 0, contoursSize = contours.size(); i < contoursSize; i++) {
            MatOfPoint m = contours.get(i);
            Rect r = Imgproc.boundingRect(m);

            double area = r.area();
            double aspectRatio = r.width / (double) r.height;

            boolean validArea = area > BackboardDetectorConstants.minArea && area < BackboardDetectorConstants.maxArea;
            boolean validAspectRatio = aspectRatio > BackboardDetectorConstants.minAspectRatio && aspectRatio < BackboardDetectorConstants.maxAspectRatio;

            if (validArea && validAspectRatio) {
                retVal.add(new PotentialBackboard(m, i));
            }
        }
        return retVal;
    }
}
