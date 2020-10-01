package com.bfr.util.math;

import static java.lang.Math.PI;

/**
 * Contains useful static math methods for general FTC use.
 * @author Alex Appleby
 */
public class FTCMath {

    /**
     * Wraps an angle to the range -2pi to 2pi
     * @param angle Angle in radians
     */
    public static double wrapAngle(double angle){
        return angle % (2 * PI);
    }

    /**
     * Ensures that an angle is expressed in the shortest distance from 0 radians as possible. Generally useful for angle differences.
     * @param angle Angle in radians.
     */
    public static double ensureIdealAngle(double angle){
        while (angle > PI){
            angle -= (2 * PI);
        }
        while (angle < - PI){
            angle += (2 * PI);
        }
        return angle;
    }

    private FTCMath(){}
}
