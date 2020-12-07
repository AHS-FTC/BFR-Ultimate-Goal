package com.bfr.control.pidf;

import com.acmerobotics.dashboard.config.Config;

@Config
public class TurnConstants {
    public static double kP = .011;
    public static double kI = 0.0;
    public static double kD = 1.0;
    public static double finishedThreshold = 1.5; //The maximum size of the error in which the turn is considered finished
    public static double minPower = 0.13; //The minimum power
}
