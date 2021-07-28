package com.bfr.control.pidf;

import com.acmerobotics.dashboard.config.Config;

@Config
public class ThresholdConstants {
    public static double min_h = 100;
    public static double min_s = 100;
    public static double min_v = 35;

    public static double max_h = 120;
    public static double max_s = 255;
    public static double max_v = 255;
    public static boolean showRaw = false;
}
