package com.bfr.hardware;

import com.bfr.control.path.Position;
import com.bfr.util.FTCUtilities;
import com.bfr.util.math.FTCMath;
import com.qualcomm.hardware.lynx.LynxModule;

import org.opencv.core.Mat;

import java.util.List;

public class Robot {
    private WestCoast westCoast = new WestCoast();
    private Position position;

    private final double POWER = .6;

    private List<LynxModule> hubs;

    public Robot() {
        hubs = FTCUtilities.getHardwareMap().getAll(LynxModule.class);

        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        position = westCoast.getPosition();
    }

    public WestCoast getWestCoast() {
        return westCoast;
    }

    public void driveToPosition(Position targetPos){

        double angleToTarget = FTCMath.ensureIdealAngle(position.angleTo(targetPos), position.heading);
        double distanceToTarget = position.distanceTo(targetPos);

        turnToHeading(angleToTarget);

        driveDistance(distanceToTarget);

        turnToHeading(targetPos.heading);
    }

    public void turnToHeading(double targetHeading){
        //turning CCW -> positive to agree with rads (and vice versa)
        double turnDir = Math.signum(targetHeading - position.heading);

        westCoast.setTankPower(turnDir * -POWER, turnDir * POWER);
        while (Math.abs(position.heading - targetHeading) > Math.toRadians(2.5) && FTCUtilities.opModeIsActive()){
            FTCUtilities.addData("heading", position.heading);
            FTCUtilities.addData("error", Math.abs(position.heading - targetHeading));
            FTCUtilities.updateTelemetry();
            update();
        }
        westCoast.brakeMotors();
    }

    /**
     * Drives straight forward or backwards using encoder values and PID.
     * @param targetDistance
     */
    public void driveDistance(double targetDistance){
        double driveDir = Math.signum(targetDistance);

        Position startPosition = new Position(0,0,0);
        startPosition.copyFrom(position);

        westCoast.setTankPower(driveDir * POWER, driveDir * POWER);
        while (startPosition.distanceTo(position) < targetDistance && FTCUtilities.opModeIsActive()){
            update();
            //todo implement pid
        }

        westCoast.brakeMotors();
    }


    /**
     * The update() method contains maintenance stuff
     * it should be called every iteration in any blocking method.
     */
    private void update(){
        //clear sensor cache
        for(LynxModule hub : hubs) {
            hub.clearBulkCache();
        }

        //run sensor reads
        position = westCoast.getPosition();
        //finish sensor reads

        //todo track loop times
    }

    public Position getPosition() {
        return position;
    }
}
