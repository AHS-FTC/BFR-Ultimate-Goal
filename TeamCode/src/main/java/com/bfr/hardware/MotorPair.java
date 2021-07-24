package com.bfr.hardware;

import com.bfr.hardware.sensors.CurrentMonitor;

public class MotorPair {
    protected final Motor m1, m2;

    public MotorPair(Motor m1, Motor m2) {
        this.m1 = m1;
        this.m2 = m2;
    }

    public void setPower(double power) {
        m1.setPower(power);
        m2.setPower(power);
    }

    public double getRotations() {
        return m1.getRotations();
    }
}
