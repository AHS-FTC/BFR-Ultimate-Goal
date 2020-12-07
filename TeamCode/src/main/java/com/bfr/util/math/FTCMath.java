package com.bfr.util.math;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

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

    /**
     * Ensures that the ideal angle is the closest possible wrapping relative to some reference angle
     */
    //todo add tests from: https://github.com/AHS-FTC/BFR-Pathfinder/commit/b7f0a25ad471195664f4f839b121fdd2a5e4940a
    public static double ensureIdealAngle(double rawAngle, double referenceAngle){
        double retAngle;
        double angleDifference = referenceAngle - rawAngle;

        //make sure angleDifference isn't more than 2pi away from the last angle
        if (Math.abs(angleDifference) > 2 * Math.PI) {
            angleDifference  %= (2 * Math.PI);
        }

        // Make sure we have the most 'efficient' relative angleDistance
        if (Math.abs(angleDifference) > Math.PI) {
            if (Math.signum(angleDifference) == 1) {
                angleDifference = (2 * Math.PI) - angleDifference;
            } else {
                angleDifference =  angleDifference + (2 * Math.PI);
            }
        }

        retAngle = referenceAngle - angleDifference;//derived from definition of angleDifference
        return retAngle;
    }

    public static double nearestToZero(double a, double b){
        if (abs(a) < abs(b)){
            return a;
        }
        return b;
    }

    private FTCMath(){}
}
