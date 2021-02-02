package com.bfr.hardware.sensors;
import com.bfr.util.FTCUtilities;
import com.qualcomm.hardware.bosch.BNO055IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * Wrapper class to manage the FTC BNO055IMU class and facilitate mocking in the future
 * @author Alex Appleby
 */
public class IMU {
    private BNO055IMU imu;
    private Orientation lastAngles = new Orientation();
    private BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
    private double lastAngle;
    private double wrappingOffset = 0;
    private double initial = 0;
    private boolean firstTime = true;
    private int flip = 1;
    private final double initialHeadingOffset;

    public IMU(String deviceName, boolean flipped, double initialHeading) {
        imu = FTCUtilities.getHardwareMap().get(BNO055IMU.class, deviceName);
        flip = flipped ? 1:-1;
        initialHeadingOffset = initialHeading;

        parameters.mode = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = false;

        imu.initialize(parameters);
    }

    /**
     * Gets the IMU Heading. Oversteps the internal IMU behavior that returns radians between PI and Math.toDegrees(-PI, meaning this method can return beyond 2PI and -2PI
     * @return Heading in radians
     */
    public double getHeading(){
        if (firstTime){
            initial = getRawHeading();
            lastAngle = initial;
            firstTime = false;
        }

        double rawAngle = getRawHeading();

        double deltaAngle = rawAngle - lastAngle;
        lastAngle = rawAngle;

        if (deltaAngle < -Math.PI){
            wrappingOffset += 2*Math.PI;
        } else if (deltaAngle> Math.PI){
            wrappingOffset -= 2*Math.PI;
        }

//        System.out.println("initial " + initial);
//        System.out.println("offset " + offset);
//        System.out.println("rawAngle " + rawAngle);
//        System.out.println("last " + lastAngle);

        double heading = rawAngle + wrappingOffset - initial;

        return heading * flip + initialHeadingOffset;
    }

    private double getRawHeading() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES); //ZYX
        return Math.toRadians(angles.firstAngle);
    }

    public boolean isCalibrated (){
        return imu.isGyroCalibrated();
    }
    /*
    public double getGlobalHeading(){
        update();
        return lastAngles.firstAngle;
    }
    */

}