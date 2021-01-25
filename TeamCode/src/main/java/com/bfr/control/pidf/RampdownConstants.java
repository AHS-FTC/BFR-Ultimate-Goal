package com.bfr.control.pidf;

import com.acmerobotics.dashboard.config.Config;
import com.bfr.hardware.WestCoast;

@Config
public class RampdownConstants {
    public static double kP = 0.3 / WestCoast.INCHES_PER_ROTATION;
    public static double kI = 0.0;
    public static double kD = 0.0;
    //The maximum size of the error in which driveStraight() is considered finished.
    public static double finishedThreshold = 0.1;
    public static double minPower = 0.15;
}
