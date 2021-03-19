package com.bfr.hardware.sensors;

import com.bfr.util.AllianceColor;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.Point;

/**
 * A system of MB12442 distance sensors that work in conjunction
 */
public class MB1242System {
    private MB1242DistanceSensor frontSensor, leftSensor;

    private static final double INCHES_PER_CM = 0.393701;
    private static final double FRONT_OFFSET = -3.7, LEFT_OFFSET = .75;

    public MB1242System() {
        frontSensor = FTCUtilities.getHardwareMap().get(MB1242DistanceSensor.class, "dist_front");
        leftSensor = FTCUtilities.getHardwareMap().get(MB1242DistanceSensor.class, "dist_left");
    }

    public void doPings(){
        frontSensor.pingDistance();
        leftSensor.pingDistance();
    }

    /**
     * @return point on the field, relative to walls
     */
    public Point doReads(){
        double front = frontSensor.readDistance() * INCHES_PER_CM + FRONT_OFFSET;
        double left = leftSensor.readDistance() * INCHES_PER_CM + LEFT_OFFSET;

        if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
            left *= -1;
        }

        return new Point(left, front);
    }
}
