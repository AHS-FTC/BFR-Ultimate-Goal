package com.bfr.control.vision;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class VisionUtil {

    public enum  HSVChannel{
        HUE(0),
        SATURATION(1),
        VALUE(2);

        public final int index;

        HSVChannel(int index) {
            this.index = index;
        }
    }

    /**
     * Finds the average value of a channel in a particular region of a Mat.
     * Input mat must be in HSV form.
     * @param mat input
     * @param x the x value of the top right corner
     * @param y the x value of the top right corner
     * @param width region width
     * @param height region height
     * @param hsvChannel the channel being averaged.
     */
    public static double findAvgOfRegion(Mat mat, int x, int y, int width, int height, HSVChannel hsvChannel){
        Rect cropRect = new Rect(x, y, width, height);

        Mat roi = mat.submat(cropRect);

        List<Mat> channels = new ArrayList<>();

        Core.split(roi, channels);

        Mat channel = channels.get(hsvChannel.index);
        Scalar meanScalar = Core.mean(channel);

        roi.release();
        channel.release();

        return meanScalar.val[0];
    }

    public static MatOfPoint findLargestContour(List<MatOfPoint> contours){
        MatOfPoint largestContour = contours.get(0);
        double largestArea = Imgproc.contourArea(largestContour);

        for (int i = 1; i < contours.size(); i++) {
            MatOfPoint currentContour = contours.get(i);
            double currentArea = Imgproc.contourArea(currentContour);

            if(currentArea > largestArea){
                largestContour = currentContour;
                largestArea = currentArea;
            }
        }

        return largestContour;
    }

    /**
     * Takes a cropped, binary mat and finds the corners of the vision target
     */
    public static void findViewpointCorners(Mat input){
        Mat inverted = new Mat();
        Core.bitwise_not(input, inverted);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(inverted, contours, new Mat(),  Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        //convert largest contour to matOfPoint2f
        MatOfPoint2f largestContour = new MatOfPoint2f(findLargestContour(contours).toArray());
        MatOfPoint2f approx = new MatOfPoint2f();

        double epsilon = 0.05 * Imgproc.arcLength(largestContour, true);
        Imgproc.approxPolyDP(largestContour, approx, epsilon, true);
        Point[] points = approx.toArray();

        assert points.length == 4;

        for (int i = 0; i < points.length; i++) {
            MatOfPoint pointMat= new MatOfPoint(points);
            List<MatOfPoint> list = new ArrayList<>();
            list.add(pointMat);

            Imgproc.polylines(input, list, true, new Scalar(100), 1);
            Imgproc.circle(input, points[i], 1, new Scalar(50), 1);
        }

        inverted.release();
        largestContour.release();
        approx.release();
    }


    private VisionUtil() {}
}
