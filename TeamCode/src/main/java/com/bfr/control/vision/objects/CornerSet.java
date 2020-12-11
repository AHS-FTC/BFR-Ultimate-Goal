package com.bfr.control.vision.objects;

import com.bfr.control.vision.VisionException;
import com.bfr.util.FTCUtilities;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vision object for a set of corners
 */
public class CornerSet extends VisionObject{
    //todo make this not leek all over the place
    private Mat original;

    enum CornerType {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    //protected for testing
    Map<CornerType, Corner> corners = new HashMap<>();

    public void make(Target target, Backboard backboard, Mat original) throws VisionException {
        this.original = original;
        List<Point> points = target.approxCorners;
        List<Corner> corners = new ArrayList<>();
        for (Point p : points) {
            Corner c = new Corner(p);
            c.make(backboard);
            corners.add(c);
        }

        if(points.size() != 4) {
            throw new VisionException("Didn't detect 4 estimated points in target.");
        }

        assignCorners(corners);
    }

    /**
     * Takes an unordered list of 4 corners and assigns them to the top left, top right, bottom left, bottom right fields.
     * @throws VisionException if things are sus.
     * Package protected for testing.
     */
    void assignCorners(List<Corner> unorderedCorners) throws VisionException {
        List<Corner> topCorners = new ArrayList<>();
        List<Corner> bottomCorners = new ArrayList<>();

        /*
        find the average y value of all corners. Sort the corners above the mean into top and below the mean into bottom.
        This algorithm will definitely not work under all possible sets of four corners, but it should always work in a realistic
        scenario, where the bottom two corners and the top two corners are easily discernible from the mean.
        If this fails in practice we most certainly have bad data and we'd like to dispose of the results.
        */

        double sum = 0.0;
        for (Corner c : unorderedCorners) {
            sum += c.point.y;
        }
        double avg = sum / unorderedCorners.size();

        for (Corner c : unorderedCorners) {
            //note that lower y == higher in image
            if(c.point.y < avg){
                topCorners.add(c);
            } else {
                bottomCorners.add(c);
            }
        }

        if(topCorners.size() != 2 && bottomCorners.size() != 2){
            throw new VisionException("Estimate corners were not easily categorized, an issue likely occurred.");
        }

        //greater x = righter
        if (topCorners.get(0).point.x > topCorners.get(1).point.x){
            corners.put(CornerType.TOP_RIGHT, topCorners.get(0));
            corners.put(CornerType.TOP_LEFT, topCorners.get(1));
        } else {
            corners.put(CornerType.TOP_RIGHT, topCorners.get(1));
            corners.put(CornerType.TOP_LEFT, topCorners.get(0));
        }

        //greater x = righter
        if (bottomCorners.get(0).point.x > bottomCorners.get(1).point.x){
            corners.put(CornerType.BOTTOM_RIGHT, bottomCorners.get(0));
            corners.put(CornerType.BOTTOM_LEFT, bottomCorners.get(1));
        } else {
            corners.put(CornerType.BOTTOM_RIGHT, bottomCorners.get(1));
            corners.put(CornerType.BOTTOM_LEFT, bottomCorners.get(0));
        }
    }


    @Override
    public void release() {

    }

    @Override
    public void dump() {
        String directory = "vision_dump/corners/";

        Mat out = new Mat();
        original.copyTo(out);

        for (CornerType type : corners.keySet()) {
            Corner c = corners.get(type);

            FTCUtilities.saveImage(c.getMat(), directory + type.toString() + ".jpg", Imgproc.COLOR_GRAY2BGR);

            Imgproc.circle(out, c.point, 3, new Scalar(55, 255, 255),1);
        }

        FTCUtilities.saveImage(out, directory + "corners.jpg", Imgproc.COLOR_HSV2BGR);
    }
}
