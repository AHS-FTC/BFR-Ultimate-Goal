package com.bfr.control.vision.objects;

import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.VisionUtil;

import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Powershots {

    public final Map<Position, Double> xPositionsMap = new HashMap<>();

    public enum Position {
        LEFT,
        MID,
        RIGHT
    }

    public Powershots(List<MatOfPoint> powershotContours) throws VisionException{
        if(powershotContours.size() < 3){
            throw new VisionException("Unable to find three or more contours when looking for powershots");
        }

        //find three largest contours
        List<PotentialContour> potentialContours = new ArrayList<>();

        for (MatOfPoint contour : powershotContours) {
            potentialContours.add(new PotentialContour(contour));
        }

        //sort in descending area
        Collections.sort(potentialContours, Collections.reverseOrder());

        //grab the 3 largest contours
        double[] xPositions = new double[3];

        for (int i = 0; i <= 2; i++) {
            Moments m = Imgproc.moments(potentialContours.get(i).contour);

            //x value of centroid
            double x = m.get_m10() / m.get_m00();

            xPositions[i] = x;
        }


        //sort xPositions ascending
        Arrays.sort(xPositions);

        //place into hashmap
        xPositionsMap.put(Position.LEFT, xPositions[0]);
        xPositionsMap.put(Position.MID, xPositions[1]);
        xPositionsMap.put(Position.RIGHT, xPositions[2]);

        //avoid memory leak
        VisionUtil.emptyContourList(powershotContours);
    }

    private static class PotentialContour implements Comparable<PotentialContour> {

        private final double area;
        private final MatOfPoint contour;

        public PotentialContour(MatOfPoint contour) {
            this.contour = contour;
            area = Imgproc.contourArea(contour);
        }

        @Override
        public int compareTo(PotentialContour potentialContour) {

            //see docs for Comparable
            return (int) (this.area - potentialContour.area);
        }
    }
}
