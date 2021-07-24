package com.bfr.hardware.sensors;

import com.bfr.hardware.MotorPair;

import java.util.HashMap;
import java.util.Map;

public class CurrentMonitor {
    private static Map<MotorPair, Double> currentRegistry = new HashMap<>();

    public static void registerCurrent(MotorPair motorPair, double current) {
        currentRegistry.put(motorPair, current);
    }

    public static double findTotalCurrent() {
        double sum = 0;

        for (Double current : currentRegistry.values()) {
            sum += current;
        }

        return sum;
    }

    public static double findMultiplier() {
        double current = findTotalCurrent();

        //math: https://www.desmos.com/calculator/6wrrfwuh0d
        if (current > 25) {
            return Math.exp(-0.3 * (current - 25));
        } else {
            return 1.0;
        }
    }
}
