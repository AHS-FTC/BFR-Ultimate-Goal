package com.bfr.control.vision.objects;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bfr.control.pidf.ThresholdConstants;
import com.bfr.control.vision.BackboardDetector;
import com.bfr.control.vision.BackboardThresholdPipeline;
import com.bfr.control.vision.MTIVisionBridge;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionUtil;
import com.bfr.util.AllianceColor;
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
import java.util.Arrays;
import java.util.List;

public class MTIBackboardDetectionPipeline extends OpenCvPipeline {
    private MatOfPoint referenceContour = new MatOfPoint();
    private Mat referenceMat = new Mat();
    private BackboardThresholdPipeline backboardThresholdPipeline = new BackboardThresholdPipeline();
    private List<MatOfPoint> contours = new ArrayList<>();
    private List<PotentialBackboard> potentialBackboards = new ArrayList<>();

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
        Imgproc.findContours(thresholded, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        potentialBackboards = BackboardDetector.validateContours(contours);

        if (potentialBackboards.size() == 0 ) {
            MTIVisionBridge.instance.setGoalVisible(false);

            if(ThresholdConstants.showRaw) {
                return input;
            } else {
                return out;
            }
        }

        for (PotentialBackboard potentialBackboard : potentialBackboards) {
            //draw potential backboards
            Rect boundingBox = Imgproc.boundingRect(potentialBackboard.contour);

            Imgproc.rectangle(out, boundingBox, new Scalar(0, 255, 0), 3);

            List<MatOfPoint> subcontours = findSubcontours(potentialBackboard.index);
            potentialBackboard.subContours = subcontours;
            Imgproc.drawContours(out, subcontours, -1, new Scalar(0, 0, 255), 3);

            Imgproc.putText(out, "numSides: " + potentialBackboard.numSides, boundingBox.br(), 0, 1, new Scalar(0, 255, 0), 3);
        }

        PotentialBackboard finalBackboard;
        if (potentialBackboards.size() == 1) {
            finalBackboard = potentialBackboards.get(0);
        } else {
            finalBackboard = getMostViablePotentialBackboard(potentialBackboards);

        }

        if(finalBackboard.subContours.size() == 0 ) {
            MTIVisionBridge.instance.setAngleToGoal(finalBackboard.contour);
        } else {
            try {
                MTIVisionBridge.instance.setAngleToGoal(VisionUtil.findLargestContour(finalBackboard.subContours));
            } catch (VisionException e) {
                e.printStackTrace();
            }
        }

        MTIVisionBridge.instance.setGoalVisible(true);


        if(ThresholdConstants.showRaw) {
            return input;
        } else {
            return out;
        }

    }

    private List<MatOfPoint> findSubcontours(int index) {
        List<MatOfPoint> retVal = new ArrayList<>();

        for (int i = 0, cols = hierarchy.cols(); i < cols; i++) {
            int[] data = new int[4];

            hierarchy.get(0, i, data);

            System.out.println(Arrays.toString(data));

            if(data[3] == index) {
                retVal.add(contours.get(i));
            }
        }

        return retVal;
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

    public void setColor(AllianceColor color) {
        if (color.equals(AllianceColor.BLUE)) {
            backboardThresholdPipeline.setHue(110);
        } else {
            backboardThresholdPipeline.setHue(0);
        }
    }
}
