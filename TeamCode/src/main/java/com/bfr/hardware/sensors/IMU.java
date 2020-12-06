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
    private double offset = 0;
    private double initial = 0;
    private boolean firstTime = true;
    private int flip = 1;

    public IMU(String deviceName, boolean flipped) {
        imu = FTCUtilities.getHardwareMap().get(BNO055IMU.class, deviceName);
        flip = flipped ? 1:-1;

        parameters.mode = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = false;

        imu.initialize(parameters);


    }

    /**
     * Gets the IMU Heading. Oversteps the internal IMU behavior that returns degrees between 180 and -180, meaning this method can return beyond 360 and -360
     * @return Heading in degrees
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

        if (deltaAngle < -180){
            offset+=360;
        } else if (deltaAngle>180){
            offset -=360;
        }

//        System.out.println("initial " + initial);
//        System.out.println("offset " + offset);
//        System.out.println("rawAngle " + rawAngle);
//        System.out.println("last " + lastAngle);

        double heading = rawAngle + offset - initial;

        return heading * flip;
    }

    private double getRawHeading() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES); //ZYX
        return angles.firstAngle;
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