package com.bfr.hardware;

import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public class Motor {

    protected boolean flipped;
    protected String deviceName;
    protected DcMotorEx motor;
    protected double currentMotorPower = 0;
    private double ticksPerRotation;

    //sometimes the encoder disagrees with the motor.
    private int encoderFlip = 1;

    public Motor(String deviceName, double ticksPerRotation, boolean flipped) {
        this.flipped = flipped;
        this.deviceName = deviceName;
        this.ticksPerRotation = ticksPerRotation;

        motor = FTCUtilities.getHardwareMap().get(DcMotorEx.class, deviceName);
        motor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        motor.setCurrentAlert(6.0, CurrentUnit.AMPS);

        if (flipped) {
            motor.setDirection(DcMotorEx.Direction.REVERSE);
        } else {
            motor.setDirection(DcMotorEx.Direction.FORWARD);
        }
    }

    public void setPower(double newMotorPower) {
        if (newMotorPower != currentMotorPower) {
            motor.setPower(Range.clip(newMotorPower, -1.0, 1.0));
            currentMotorPower = newMotorPower;
        }
    }

    public boolean isOverCurrent(){
        return motor.isOverCurrent();
    }

    public void zeroDistance() {
        DcMotorEx.RunMode previousRunMode = motor.getMode();
        motor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(previousRunMode);
    }

    public void setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior behavior) {
        motor.setZeroPowerBehavior(behavior);
    }

    public void flipEncoder(){
        encoderFlip = -1;
    }

    public double getRotations() {
        return encoderFlip * (motor.getCurrentPosition() / ticksPerRotation);
    }

    public double getCurrent() {
        return motor.getCurrent(CurrentUnit.AMPS);
    }
}
