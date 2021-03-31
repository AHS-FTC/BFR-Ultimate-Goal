package com.bfr.control.pidf;

import com.acmerobotics.dashboard.config.Config;

@Config
public class ShooterConstants {
    public static double kP = 0.002;
    public static double kD = 0;

    public static double standardRPM = 2800;
    public static double standardFeedforward = 0.71;

    public static double powerShotFeedforward = 0.63;
    public static double powerShotRPM = 2500.0;

    public static double farFeedForward = .72;
    public static double farRPM = 2775;

}
