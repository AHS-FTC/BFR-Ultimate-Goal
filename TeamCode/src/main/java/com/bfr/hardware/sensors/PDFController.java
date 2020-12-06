package com.bfr.hardware.sensors;

public class PDFController {
    private final double Kp, Kd, Kf;
    private final double setPoint;

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

        double derivative = error - lastError;

        lastError = error;
        return (Kp * error) + (Kd *  derivative) + Kf;
    }
}
