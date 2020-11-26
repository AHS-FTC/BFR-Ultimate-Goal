package com.bfr.hardware.sensors;

import com.bfr.util.Constants;
import com.bfr.util.FTCUtilities;

public class PDFController {
    private final double Kp, Kd, Kf;
    private final double setPoint;
    private long lastTime = FTCUtilities.getCurrentTimeMillis();

    private double lastError;

    public PDFController(double Kp, double Kd, double Kf, double setPoint, double initialValue) {
        this.Kp = Kp;
        this.Kf = Kf;
        this.Kd = Kd;
        this.setPoint = setPoint;

        lastError = setPoint - initialValue;
    }

    public double getOutput(double current){
        double error = setPoint - current;

        long currentTime = FTCUtilities.getCurrentTimeMillis();
        long deltaTime = currentTime - lastTime;

        double derivative = (error - lastError)/deltaTime;

        lastError = error;
        lastTime = currentTime;
        return (Constants.p * error) + (Constants.d *  derivative) + Kf;
    }
}
