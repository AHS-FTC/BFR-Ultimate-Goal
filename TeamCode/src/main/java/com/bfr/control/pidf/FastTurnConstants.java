package com.bfr.control.pidf;

import com.acmerobotics.dashboard.config.Config;

@Config
public class FastTurnConstants {
    private static final double CONVERSION_CONSTANT = 180.0/Math.PI;
    public static double kP = .011 * CONVERSION_CONSTANT;
    public static double kI = 0.0;
    public static double kD = 1.0 * CONVERSION_CONSTANT;
    public static double finishedThreshold = 1.5/CONVERSION_CONSTANT; //The maximum size of the error in which the turn is considered finished
    public static double minPower = 0.13; //The minimum power
    public static double maxPower = 0.8;
}
