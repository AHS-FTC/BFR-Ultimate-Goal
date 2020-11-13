package com.bfr.control.vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Crops a thresholded backboard to it's bounding box.
 */
public class BackboardCropPipeline extends OpenCvPipeline {

    /**
     * How much of the bounding box should we keep?
     * We'd like to crop out most of the high goal.
     */
    private static double heightRatio = 2.0 / 3.0;

    @Override
    public Mat processFrame(Mat input) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(input, contours, hierarchy,  Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint largestContour = VisionUtil.findLargestContour(contours);

        Rect boundingRect = Imgproc.boundingRect(largestContour);
        double newHeight = boundingRect.height * heightRatio;
        boundingRect.height = (int) newHeight;

        Mat out = input.submat(boundingRect);

        largestContour.release();
        return out;
    }
}
