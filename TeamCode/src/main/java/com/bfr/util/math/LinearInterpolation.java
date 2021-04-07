package com.bfr.util.math;

import java.util.List;

public class LinearInterpolation {
    private List<Point> model;

    public LinearInterpolation(List<Point> model){
        this.model = model;
    }

    /**
     * Takes in any given x and returns a y through linear interpolation
     * @param x
     * @return interpolated y
     */
    public double interpolatePower(double x){
        Point prev = model.get(0);
        for (int i = 1; i < model.size(); i++) {
            Point curr = model.get(i);
            double lowerX = prev.x;
            double upperX = curr.x;

            //Find interpolated point and calculate y
            if (lowerX < x && x <= upperX){
                double totalXDiff = upperX - lowerX;
                double targetXDiff = upperX - x;

                //Ratio <= 1
                double ratio = targetXDiff/totalXDiff;

                //Find y difference
                double yDiff = curr.y - prev.y;

                //Multiply ratio by y difference and add lower y
                return yDiff * ratio + prev.y;
            }
            prev = curr;
        }
        throw new IllegalArgumentException("Make sure x is within the bounds of the model");
    }
}
