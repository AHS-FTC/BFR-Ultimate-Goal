package com.bfr.control.pidf;

import com.acmerobotics.dashboard.config.Config;
import com.bfr.hardware.WestCoast;

@Config
public class AccurateRampdownConstants {
    public static double kP = .035;
    public static double kI = 0.0;
    public static double kD = 11.0;

    //The maximum size of the error in which driveStraight() is considered finished.
    public static double finishedThreshold = 0.1;
    public static double minPower = .1;
}
