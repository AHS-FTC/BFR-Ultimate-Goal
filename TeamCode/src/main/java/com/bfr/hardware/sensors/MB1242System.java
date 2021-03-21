package com.bfr.hardware.sensors;

import com.bfr.control.path.Position;
import com.bfr.util.AllianceColor;
import com.bfr.util.FTCUtilities;
import com.bfr.util.loggers.ControlCenter;
import com.bfr.util.math.Point;

/**
 * A system of MB12442 distance sensors that work in conjunction with an OdometrySystem, regularly correcting them.
 */
public class MB1242System {
    private MB1242DistanceSensor frontSensor, leftSensor;

    private Odometry odometry;

    private static final double INCHES_PER_CM = 0.393701;
    private static final double FRONT_OFFSET = -3.7, LEFT_OFFSET = .75;

    public MB1242System(Odometry odometry) {
        frontSensor = FTCUtilities.getHardwareMap().get(MB1242DistanceSensor.class, "dist_front");
        leftSensor = FTCUtilities.getHardwareMap().get(MB1242DistanceSensor.class, "dist_left");

        this.odometry = odometry;
    }

    public void doPings(){
        frontSensor.pingDistance();
        leftSensor.pingDistance();
    }

    /**
     * reads sensors and corrects odometry acccordingly
     */
    public void doReads(){
        int frontRaw = frontSensor.readDistance();
        int leftRaw = leftSensor.readDistance();

        if(frontRaw < 5.0 || leftRaw < 5.0){
            ControlCenter.addNotice("Detected anomalous mb1242 readings");
            //don't update position with anomalous readings
            return;
        }

        double y = frontRaw * INCHES_PER_CM + FRONT_OFFSET;
        double x = leftRaw * INCHES_PER_CM + LEFT_OFFSET;

        if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
            x *= -1;
        }

        Position p = new Position(x, y, odometry.getPosition().heading);

        odometry.setPosition(p);
    }
}
