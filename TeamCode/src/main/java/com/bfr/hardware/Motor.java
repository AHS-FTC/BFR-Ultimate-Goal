package com.bfr.hardware;

import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

public class Motor {

    protected boolean flipped;
    protected String deviceName;
    protected DcMotor motor;
    private double previousMotorPower = 0;
    private double ticksPerRotation;

    public Motor(String deviceName, double ticksPerRotation, boolean flipped) {
        this.flipped = flipped;
        this.deviceName = deviceName;
        this.ticksPerRotation = ticksPerRotation;

        motor = FTCUtilities.getHardwareMap().get(DcMotor.class, deviceName);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        if (flipped) {
            motor.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            motor.setDirection(DcMotorSimple.Direction.FORWARD);
        }
    }

    public void setPower(double motorPower) {
        if (motorPower != previousMotorPower) {
            motor.setPower(Range.clip(motorPower, -1.0, 1.0));
            previousMotorPower = motorPower;
        }
    }

    public void zeroDistance() {
        DcMotor.RunMode previousRunMode = motor.getMode();
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(previousRunMode);
    }

    public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
        motor.setZeroPowerBehavior(behavior);
    }

    public double getRotations() {
        return (motor.getCurrentPosition()/ticksPerRotation);
    }

}
