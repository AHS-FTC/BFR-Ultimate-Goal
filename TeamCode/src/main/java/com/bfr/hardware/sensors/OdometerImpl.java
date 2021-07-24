package com.bfr.hardware.sensors;

import com.bfr.hardware.Motor;

/**
 *  Reads encoder values, tracks, and returns distance based on wheel diameter.
 *  <b>IMPORTANT:</b> setDirection() method on DcMotor changes encoder direction
 * @author Alex Appleby
 */
public class OdometerImpl implements Odometer {
    private Motor motor;
    private double wheelCircumference;// in inches, used to be in mm

    /**
     * @param deviceName A string that ties the encoder sensor to the motor port of a DcMotor. Should be the same as whatever motor it's attached to.
     * @param wheelDiameter The diameter of the odometer wheel in inches. Likely needs to be tuned to reflect tolerances of wheel.
     * @param flip Whether or not this returns flipped values. Probably determine this by experimentation.
     */
    public OdometerImpl(String deviceName, double wheelDiameter, boolean flip, double ticksPerRotation) {
        motor = new Motor(deviceName, ticksPerRotation, false);

        if (flip) {
            motor.flipEncoder();
        }
        reset();
        wheelCircumference = wheelDiameter * Math.PI;
    }

    /**
     * @return Distance of rotation in inches.
     */
    public double getDistance(){
        double rotations = motor.getRotations();
        double distance = rotations * wheelCircumference; // rotations / ticks per rotation but combined for optimization
        return distance;
    }

    /**
     * Resets encoder so that getDistance yields 0.
     */
    public void reset(){
        motor.zeroDistance();
    }
}
