package com.bfr.control.pidf;

public class PIDFDataPoint {
    public final double value;
    public final long time;

    public PIDFDataPoint(double value, long time) {
        this.value = value;
        this.time = time;
    }
}
