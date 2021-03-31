package com.bfr.control.vision;

import com.acmerobotics.dashboard.config.Config;

@Config
public class StackDetectorConstants {
    public static int minHue = 0, maxHue = 100;
    public static int minSat = 50, maxSat = 255;

    public static double zeroRingAreaMax = 3000;

    public static double fourRingAreaMin = 14000;
}
