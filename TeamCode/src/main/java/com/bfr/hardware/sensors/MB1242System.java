package com.bfr.hardware.sensors;

import com.bfr.control.path.Position;
import com.bfr.util.AllianceColor;
import com.bfr.util.FTCUtilities;
import com.bfr.util.loggers.ControlCenter;
import com.bfr.util.math.FTCMath;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * A system of MB12442 distance sensors that work in conjunction with an OdometrySystem, regularly correcting them.
 */
public class MB1242System {
    private MB1242DistanceSensor frontSensor1, frontSensor2, lateralSensor1, lateralSensor2;
    private final double MINIMUM_DISTANCE = 43; //in cm
    private final double MAX_DISTANCE = 152; //in cm

    private Odometry odometry;
    private State state = State.RESTING;

    private long startTime = 0;
    private final long WAIT_TIME = 50;

    private double frontRaw1;
    private double frontRaw2;

    private double lateralRaw1;
    private double lateralRaw2;

    private static final double INCHES_PER_CM = 0.393701;
    private static final double FRONT_OFFSET = -3.7, LEFT_OFFSET = .75;

    public MB1242System(Odometry odometry) {
        HardwareMap hardwareMap = FTCUtilities.getHardwareMap();
        frontSensor1 = hardwareMap.get(MB1242DistanceSensor.class, "dist_front_1");
        frontSensor2 = hardwareMap.get(MB1242DistanceSensor.class, "dist_front_2");

        if (FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
            lateralSensor1 = hardwareMap.get(MB1242DistanceSensor.class, "dist_left_1");
            lateralSensor2 = hardwareMap.get(MB1242DistanceSensor.class, "dist_left_2");
        } if (FTCUtilities.getAllianceColor().equals(AllianceColor.RED)){
            lateralSensor1 = hardwareMap.get(MB1242DistanceSensor.class, "dist_right_1");
            lateralSensor2 = hardwareMap.get(MB1242DistanceSensor.class, "dist_right_2");
        }

        this.odometry = odometry;
    }

    private enum State{
        RESTING,
        WAIT_FOR_PING1,
        WAIT_FOR_PING2,
    }

    public boolean isResting(){
        return state.equals(State.RESTING);
    }

    /**
     * Takes the sensor values from runSystem and converts them into an odometry x,y
     * Discards any raw values that are under or over min and max distances
     */
    private void doAnalysis(){
        int susFrontRead1 = 0;
        int susFrontRead2 = 0;

        int susLateralRead1 = 0;
        int susLateralRead2 = 0;

        if (frontRaw1 <= MINIMUM_DISTANCE || frontRaw1 > MAX_DISTANCE){
            susFrontRead1++;
        }

        if (frontRaw2 <= MINIMUM_DISTANCE || frontRaw2 > MAX_DISTANCE){
            susFrontRead2++;
        }

        if (lateralRaw1 <= MINIMUM_DISTANCE || lateralRaw1 > MAX_DISTANCE){
            susLateralRead1++;
        }

        if (lateralRaw2 <= MINIMUM_DISTANCE || lateralRaw2 > MAX_DISTANCE){
            susLateralRead2++;
        }

        ControlCenter.incrementSusSensorReads(susFrontRead1 + susLateralRead1);

        double finalFrontRead;
        double finalLateralRead;

        int totalSusFront = susFrontRead1 + susFrontRead2;
        int totalSusLateral = susLateralRead1 + susLateralRead2;

        if (totalSusFront == 2){
            return;
        } else if (totalSusFront == 1){
            if (susFrontRead1 == 0){
                finalFrontRead = frontRaw1;
            } else {
                finalFrontRead = frontRaw2;
            }
        } else {
            finalFrontRead = FTCMath.furthestFromZero(frontRaw1, frontRaw2);
        }

        if (totalSusLateral == 2){
            return;
        } else if (totalSusLateral == 1){
            if (susLateralRead1 == 0){
                finalLateralRead = lateralRaw1;
            } else {
                finalLateralRead = lateralRaw2;
            }
        } else {
            finalLateralRead = FTCMath.furthestFromZero(lateralRaw1, lateralRaw2);
        }

        double y = finalFrontRead * INCHES_PER_CM + FRONT_OFFSET;
        double x = finalLateralRead * INCHES_PER_CM + LEFT_OFFSET;

        if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
            x *= -1;
        }

        Position p = new Position(x, y, odometry.getPosition().heading);

        odometry.setPosition(p);
    }

    /**
     * Non-blocking method that updates the odometry position
     */
    public void runSystem(){
        frontSensor1.pingDistance();
        lateralSensor1.pingDistance();

        startTime = FTCUtilities.getCurrentTimeMillis();
        state = State.WAIT_FOR_PING1;
    }

    public void update(){
        switch(state){
            case RESTING:
                return;
            case WAIT_FOR_PING1:
                if (FTCUtilities.getCurrentTimeMillis() - startTime > WAIT_TIME) {
                    frontRaw1 = frontSensor1.readDistance();
                    lateralRaw1 = lateralSensor1.readDistance();

                    frontSensor2.pingDistance();
                    lateralSensor2.pingDistance();

                    startTime = FTCUtilities.getCurrentTimeMillis();
                    state = State.WAIT_FOR_PING2;
                }
                break;
            case WAIT_FOR_PING2:
                if (FTCUtilities.getCurrentTimeMillis() - startTime > WAIT_TIME){
                    frontRaw2 = frontSensor2.readDistance();
                    lateralRaw2 = lateralSensor2.readDistance();

                    doAnalysis();
                    state = State.RESTING;
                }
                break;
        }
    }
}
