package com.bfr.control.teleop;

public class NormalControlCurve {
    private final double peakX, tailY, width;
    private final int flip;

    public NormalControlCurve(double peakX, double tailY, double width, boolean isNegative) {
        this.peakX = peakX;
        this.tailY = tailY;
        this.width = width;

        if(isNegative) {
            flip = -1;
        } else {
            flip = 1;
        }
    }

    public double eval(double x) {
        return flip * (1.0 - Math.abs(tailY)) * Math.exp((-(x - peakX) * (x - peakX)) / width) + tailY;
    }
}
