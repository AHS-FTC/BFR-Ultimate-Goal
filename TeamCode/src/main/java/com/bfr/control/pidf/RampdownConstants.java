package com.bfr.control.pidf;

import com.acmerobotics.dashboard.config.Config;

@Config
public class RampdownConstants {
    public static double kP = 0.7;
    public static double kI = 0.0;
    public static double kD = 0.0;
    //The maximum size of the error in which driveStraight() is considered finished.
    public static double finishedThreshold = 0.01;
}
