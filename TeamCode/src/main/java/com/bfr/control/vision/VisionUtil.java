package com.bfr.control.vision;

import com.bfr.util.FTCUtilities;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class VisionUtil {

    /**
     * Prevents potential memory leak with contour lists
     */
    public static void emptyContourList(List<MatOfPoint> contours){
        for (MatOfPoint m : contours) {
            m.release();
        }
        contours.clear();

    }

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
        FTCUtilities.saveImage(roi);

        Mat channel = getHSVChannel(roi, hsvChannel);
        Scalar meanScalar = Core.mean(channel);

        roi.release();
        channel.release();

        return meanScalar.val[0];
    }

    /**
     * Grabs one channel of an HSV image
     */
    public static Mat getHSVChannel(Mat input, HSVChannel hsvChannel){
        List<Mat> channels = new ArrayList<>();
        Core.split(input, channels);
        return channels.get(hsvChannel.index);
    }

    public static MatOfPoint findLargestContour(List<MatOfPoint> contours) throws VisionException{
        if(contours.size() == 0){
            throw new VisionException("No contours found");
        }

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
     * Adds 'padding' to a rect, adding space on all sides
     * @param padding how many pixels we should add on each side
     */
    public static void padRect(Rect rect, int padding){
        rect.x -= padding;
        rect.y -= padding;

        rect.width += (2 * padding);
        rect.height += (2 * padding);
    }

    public static Rect rectAroundPoint(Point point, int padding){
        Rect rect = new Rect(point, new Size(0,0));
        padRect(rect, padding);
        return rect;
    }


    private VisionUtil() {}
}
