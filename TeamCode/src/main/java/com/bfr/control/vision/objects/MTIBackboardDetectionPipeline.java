package com.bfr.control.vision.objects;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bfr.control.vision.BackboardDetector;
import com.bfr.control.vision.BackboardThresholdPipeline;
import com.bfr.control.vision.MTIVisionBridge;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionUtil;
import com.bfr.util.FTCUtilities;

import org.firstinspires.ftc.teamcode.R;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MTIBackboardDetectionPipeline extends OpenCvPipeline {
    private MatOfPoint referenceContour = new MatOfPoint();
    private Mat referenceMat = new Mat();
    private BackboardThresholdPipeline backboardThresholdPipeline = new BackboardThresholdPipeline();
    private List<MatOfPoint> contours = new ArrayList<>();
    private List<MatOfPoint> validContours = new ArrayList<>();
    private MatOfPoint goalContour = new MatOfPoint();

    private Mat hsv = new Mat();
    private Mat thresholded = new Mat();
    private Mat hierarchy = new Mat();
    private Mat out = new Mat();

    public MTIBackboardDetectionPipeline() {
        Resources r = FTCUtilities.getHardwareMap().appContext.getResources();
        Bitmap bMap = BitmapFactory.decodeResource(r ,R.raw.backboard);

        Utils.bitmapToMat(bMap, referenceMat);
        Imgproc.cvtColor(referenceMat, referenceMat, Imgproc.COLOR_RGB2GRAY);

        List<MatOfPoint> allContours = new ArrayList<>();

        Mat refHierarchy = new Mat();
        Imgproc.findContours(referenceMat, allContours, refHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        try {
            VisionUtil.findLargestContour(allContours).copyTo(referenceContour);
        } catch (VisionException e) {
            throw new Error(e);
        }

        refHierarchy.release();
        VisionUtil.emptyContourList(allContours);
    }

    @Override
    public Mat processFrame(Mat input) {
        Imgproc.cvtColor(input, hsv, Imgproc.COLOR_RGB2HSV);

        thresholded.release();
        thresholded = backboardThresholdPipeline.processFrame(hsv);
        Imgproc.cvtColor(thresholded, out, Imgproc.COLOR_GRAY2RGB);

        VisionUtil.emptyContourList(contours);
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        VisionUtil.emptyContourList(validContours);
        validContours = BackboardDetector.validateContours(contours);


        if (validContours.size() == 0 ) {
            MTIVisionBridge.instance.setGoalVisible(false);
            return out;
        }

        if (validContours.size() == 1) {
            goalContour = validContours.get(0);
        } else {
            List<PotentialBackboard> allBackboards = new ArrayList<>();
            for (MatOfPoint contour : validContours) {
                Rect boundingBox = Imgproc.boundingRect(contour);

                PotentialBackboard potentialBackboard = new PotentialBackboard(contour);
                allBackboards.add(potentialBackboard);

                Imgproc.rectangle(out, boundingBox, new Scalar(0, 255, 0), 3);

                Imgproc.putText(out, "numSides: " + potentialBackboard.numSides, boundingBox.br(), 0, 1, new Scalar(0, 255, 0), 3);
                potentialBackboard.release();
            }
            goalContour = getMostViablePotentialBackboard(allBackboards).contour;
            Imgproc.drawContours(out, PotentialBackboard.drawableContours, -1, new Scalar(255, 0, 0), 3);
            VisionUtil.emptyContourList(PotentialBackboard.drawableContours);
        }

        MTIVisionBridge.instance.setAngleToGoal(goalContour);

        MTIVisionBridge.instance.setGoalVisible(true);

        return out;
    }

    //finds the potential backboard that has the closest to 8 sides.
    static PotentialBackboard getMostViablePotentialBackboard(List<PotentialBackboard> potentialBackboards){
        int minSidesError = Integer.MAX_VALUE;
        PotentialBackboard mostViableBackBoard = null;

        for (PotentialBackboard backboard : potentialBackboards) {
            //how close is this polygon approximation to 8 sides?
            int sidesError = Math.abs(8 - backboard.numSides);
            if(sidesError < minSidesError) {
                minSidesError = sidesError;
                mostViableBackBoard = backboard;
            }
        }

        return mostViableBackBoard;
    }
}
