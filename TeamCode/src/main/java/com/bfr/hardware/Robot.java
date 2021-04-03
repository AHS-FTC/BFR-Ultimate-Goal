package com.bfr.hardware;


import com.acmerobotics.dashboard.FtcDashboard;
import com.bfr.control.path.Position;
import com.bfr.control.vision.Cam;
import com.bfr.control.vision.StackDetector;
import com.bfr.control.vision.VisionException;
import com.bfr.control.vision.objects.Backboard;
import com.bfr.hardware.sensors.DifOdometry;
import com.bfr.hardware.sensors.IMU;
import com.bfr.hardware.sensors.MB1242System;
import com.bfr.hardware.sensors.OdometerImpl;
import com.bfr.hardware.sensors.Odometry;
import com.bfr.util.AllianceColor;
import com.bfr.util.FTCUtilities;
import com.bfr.util.loggers.ControlCenter;
import com.bfr.util.math.FTCMath;
import com.bfr.util.math.Point;
import com.qualcomm.hardware.lynx.LynxModule;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Mat;

import java.util.List;

public class Robot {
    private WestCoast westCoast;
    private Shooter shooter = new Shooter();
    private Intake intake = new Intake();
    private WobbleArm wobbleArm = new WobbleArm();

    private MB1242System mb1242System;
    private SerialServo brolafActuator;
    private IMU imu;
    private Telemetry dashboardTelemetry = FtcDashboard.getInstance().getTelemetry();

    private Odometry odometry;

    private Point shootingPoint = new Point(-42,66); //BLUE
    private Point intakingPoint = new Point(-42,20); //BLUE

    //vision stuff
    private Cam cam;
    private Backboard backboard = new Backboard();
    private Mat latestFrame = new Mat();
    private StackDetector stackDetector;
    private StackDetector.FieldConfiguration fieldConfiguration;
    private long stackDetectorStartTime;

    private Position position;

    private boolean nextCycleState = false;

    private List<LynxModule> hubs;
    private State state = Robot.State.FREE;
    private CycleState cycleState = CycleState.TURNING_TO_INTAKE;
    private GoToHomeState homeState = GoToHomeState.TURNING_TO_HOME;

    public enum State {
        FREE,
        AUTO_CYCLE,
        TURN_TO_SHOOT,
        GO_TO_HOME,
        DETECTING_STACK,
        SQUARE_UP
    }

    private enum GoToHomeState {
        TURNING_TO_HOME,
    }

    private enum CycleState {
        TURNING_TO_INTAKE,
        INTAKING,
        WAITING_FOR_SENSORS,
        TURNING_BACK,
        DRIVING_BACK,
        AIMING,
        SHOOTING,
        TURNING_FORWARD,
        DRIVING_FORWARD,

        //powershot modes
        TURN_TO_SHOT_1,
        PSHOT_1,
        TURN_TO_SHOT_2,
        PSHOT_2,
        TURN_TO_SHOT_3,
        PSHOT_3,
    }

    public Robot(Position startingPosition) {
        hubs = FTCUtilities.getHardwareMap().getAll(LynxModule.class);
        position = startingPosition;

        if (FTCUtilities.getAllianceColor().equals(AllianceColor.RED)){
            shootingPoint = new Point(42,66);
            intakingPoint = new Point(42,20);
        }

        ControlCenter.setIntakingPoint(intakingPoint);

        brolafActuator = new SerialServo("brolaf", false);
        brolafActuator.mapPosition(.7, 1);
        brolafActuator.setPosition(0);
        imu = new IMU("imu", true, Math.PI/2);
//        cam = new Cam("Webcam 1");
//        cam.start();

        wobbleArm.setState(WobbleArm.State.STORED);

        odometry = new DifOdometry(
                new OdometerImpl("l_odo", 1.885, true, 1440.0),
                new OdometerImpl("r_odo", 1.89, false, 1440.0),
                startingPosition, 15.6
        );

        mb1242System = new MB1242System(odometry);

        ControlCenter.setDifOdometry((DifOdometry) odometry);

        westCoast = new WestCoast(imu, odometry);

        odometry.start();

        stackDetector = new StackDetector();

        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

//        //calinew Position(0,0, 0);brate vision
//        cam.copyFrameTo(latestFrame);
////        FTCUtilities.saveImage(latestFrame, "yote.png");
//        double avgHue = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.HUE);
//        double sat = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.SATURATION);
//        double val = VisionUtil.findAvgOfRegion(latestFrame, 350,190,10,17, VisionUtil.HSVChannel.VALUE);
//
//        System.out.println("calibration hue: " + avgHue);
//        System.out.println("calibration sat: " + sat);
//        System.out.println("calibration val: " + val);
//
//        backboard.calibrate(avgHue, sat, val);

    }

    public Odometry getOdometry(){return odometry;}

    public Intake getIntake(){return intake;}

    public Shooter getShooter(){return shooter;}

    public Point getIntakingPoint() {
        return intakingPoint;
    }

    public MB1242System getMb1242System(){
        return mb1242System;
    }

    public SerialServo getBrolafActuator(){return brolafActuator;}

    public StackDetector.FieldConfiguration getFieldConfiguration() {
        return fieldConfiguration;
    }

    public void setState(State state){
        this.state = state;

        switch(state){
            case FREE:
                return;
            case AUTO_CYCLE:
                westCoast.setRampdownMode(WestCoast.MovementMode.FAST);
                westCoast.startTurnGlobal(-Math.PI / 2);
                shooter.setState(Shooter.ShooterState.STANDARD);
                cycleState = CycleState.TURNING_TO_INTAKE;
                break;
            case TURN_TO_SHOOT:
//                Point
//                double globalAngle = FTCMath.ensureIdealAngle(odometry.getPosition().getAsPoint().angleTo(FTCUtilities.getGoalPoint()));
//                westCoast.startTurnGlobal(globalAngle);
                break;
            case GO_TO_HOME:
                Position position = odometry.getPosition();
                double angle = FTCMath.ensureIdealAngle(position.getAsPoint().angleTo(intakingPoint), position.heading);
                westCoast.startTurnGlobal(angle);
                homeState = GoToHomeState.TURNING_TO_HOME;
                break;
            case DETECTING_STACK:
                fieldConfiguration = stackDetector.getFieldConfiguration();
                stackDetectorStartTime = FTCUtilities.getCurrentTimeMillis();
                break;
            case SQUARE_UP:
                double squareUpAngle = FTCMath.ensureIdealAngle(Math.toRadians(-90), odometry.getPosition().heading);
                westCoast.startTurnGlobal(squareUpAngle);
                brolafActuator.setPosition(1);
                intake.changeState(Intake.State.IN);
                shooter.setState(Shooter.ShooterState.STANDARD);
                cycleState = CycleState.INTAKING;
                break;
        }

    }

    public void drive(double forward, double turn){
        westCoast.arcadeDrive(forward, turn);
    }

    /**
     * Blocking drive straight method for auto only
     * @param power
     * @param targetDistance in inches
     */
    public void driveStraight(double power, double targetDistance, WestCoast.Direction direction){
        westCoast.startDriveStraight(power, targetDistance, direction);

        while (westCoast.getState().equals(WestCoast.State.DRIVE_STRAIGHT) && FTCUtilities.opModeIsActive()){
            update();
        }
        System.out.println("ds: " + westCoast.getState().equals(WestCoast.State.DRIVE_STRAIGHT));
        System.out.println("active: " + FTCUtilities.opModeIsActive());
    }

//    public void autoAim(){
//        cam.copyFrameTo(latestFrame);
//
//        try {
//            backboard.make(latestFrame);
//            double targetX = backboard.getMiddleX();
//            double angleToTarget = Cam.getAngleFromX(targetX);
//            westCoast.startTurnLocal(angleToTarget);
//            backboard.dump();
//        } catch (VisionException e){
//            e.printStackTrace();
//            System.out.println("frick");
//            backboard.dump();
//        }
//        cam.setOutputMat(backboard.binaryCropped);
//    }

    /**
     * Blocking global turn method for auto only
     * @param globalAngle
     */
    public void turnGlobal(double globalAngle){
        westCoast.startTurnGlobal(globalAngle);

        while (westCoast.getState() == WestCoast.State.POINT_TURN && FTCUtilities.opModeIsActive()){
            update();
        }
    }

    public void turnLocal(double angle){
        turnGlobal(odometry.getPosition().heading + angle);
    }

    public WestCoast getWestCoast() {
        return westCoast;
    }

    public WobbleArm getWobbleArm() {
        return wobbleArm;
    }

    public void stopAll(){
        westCoast.brakeMotors();
        shooter.setState(Shooter.ShooterState.RESTING);
        intake.changeState(Intake.State.STOPPED);
    }

    /**
     * Handles manual state changes in state machines.
     * State machines may only advance when nextCycleState was set to true in the current control loop.
     * Otherwise, nextCycleState is reset back to false.
     */
    private boolean checkNextCycleState(){
        if(nextCycleState){
            nextCycleState = false;
            return true;
        }
        return false;
    }

    public void nextCycleState(){
        nextCycleState = true;
    }

    /**
     * Blocks for a fixed amount of time, while still updating the robot every control loop
     * @param ms how long to wait (in ms)
     */
    public void sleep(long ms){
        long startTime = FTCUtilities.getCurrentTimeMillis();

        while (FTCUtilities.getCurrentTimeMillis() - startTime < ms){
            update();
        }
    }

    /**
     * The update() method contains maintenance stuff
     * it should be called every iteration in any blocking method.
     */
    public void update(){
        long nanosBefore = System.nanoTime();

        //clear sensor cache
        for(LynxModule hub : hubs) {
            hub.clearBulkCache();
        }

        long nanosAfter = System.nanoTime();
        long bulkReadTimestamp = (nanosBefore + nanosAfter) / 2;

        odometry.update();
        shooter.update(bulkReadTimestamp);

        ControlCenter.setPosition(odometry.getPosition());

        if (state.equals(State.SQUARE_UP)){
            state = State.AUTO_CYCLE;
        }

        if(state.equals(Robot.State.AUTO_CYCLE)){
            if (FTCUtilities.getController1().areSticksNonZero() && !cycleState.equals(CycleState.INTAKING)){
                state = Robot.State.FREE;
                westCoast.setState(WestCoast.State.DRIVER_CONTROL);
            }

            switch (cycleState){
                case TURNING_TO_INTAKE:
                    if(westCoast.isInDefaultMode()){
                        intake.changeState(Intake.State.IN);

                        brolafActuator.setPosition(1);
                        cycleState = CycleState.INTAKING;
                    }
                    break;
                case INTAKING:
                    if(checkNextCycleState()){

                        mb1242System.runSystem();

                        cycleState = CycleState.WAITING_FOR_SENSORS;
                    }
                    break;
                case WAITING_FOR_SENSORS:
                    if (mb1242System.isResting()){

                        //update position, because the mb1242 system updates the odometry;
                        position = odometry.getPosition();

                        dashboardTelemetry.addData("x", position.x);
                        dashboardTelemetry.addData("y", position.y);

                        //calculate the angle and distance to our target point
                        //For the angle, keep in mind the robot is moving backwards

                        double angle = shootingPoint.angleTo(position.getAsPoint());
                        angle = FTCMath.ensureIdealAngle(angle, odometry.getPosition().heading);

                        westCoast.startTurnGlobal(angle);

                        brolafActuator.setPosition(0);

                        cycleState = CycleState.TURNING_BACK;
                    }
                    break;
                case TURNING_BACK:
                    if (westCoast.isInDefaultMode()){
                        double distance = shootingPoint.distanceTo(position);

                        westCoast.startDriveStraight(.9, distance, WestCoast.Direction.REVERSE);
                        cycleState = CycleState.DRIVING_BACK;
                    }
                    break;
                case DRIVING_BACK:
                    if (westCoast.isInDefaultMode()){
                        intake.changeState(Intake.State.STOPPED);
                        if(shooter.isState(Shooter.ShooterState.POWERSHOT)){
                            double angle;
                            if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
                                angle = Math.toRadians(-93);
                            } else {
                                angle = Math.toRadians(-90);
                            }
                            westCoast.startTurnGlobal(angle);
                            cycleState = CycleState.TURN_TO_SHOT_1;
                        } else {
                            if (FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
                                westCoast.startTurnGlobal(Math.toRadians(-82));
                            } else {
                                westCoast.startTurnGlobal(Math.toRadians(-101));
                            }
                            cycleState = CycleState.AIMING;
                        }
                    }
                    break;
                case AIMING:
                    if (westCoast.isInDefaultMode()){
                        shooter.runIndexerServos();
                        cycleState = CycleState.SHOOTING;
                    }
                    break;
                case SHOOTING:
                case PSHOT_3:
                    if(shooter.areIndexerServosResting()){
                        westCoast.startTurnGlobal(-Math.PI / 2.0);
                        shooter.setState(Shooter.ShooterState.STANDARD);
                        cycleState = CycleState.TURNING_FORWARD;
                    }
                    break;
                case TURNING_FORWARD:
                    if (westCoast.isInDefaultMode()){
                        intake.changeState(Intake.State.IN);

                        double distance = shootingPoint.distanceTo(intakingPoint);
                        westCoast.startDriveStraight(.9, distance, WestCoast.Direction.FORWARDS);
                        cycleState = CycleState.DRIVING_FORWARD;
                    }
                    break;
                case DRIVING_FORWARD:
                    if(westCoast.isInDefaultMode()){
                        westCoast.startTurnGlobal(-Math.PI / 2.0);
                        cycleState = CycleState.TURNING_TO_INTAKE;
                    }
                    break;
                //powershot states
                case TURN_TO_SHOT_1:
                    if(westCoast.isInDefaultMode()){
                        shooter.runIndexerServos();
                        cycleState = CycleState.PSHOT_1;
                    }
                    break;
                case PSHOT_1:
                    if(shooter.areIndexerServosResting()){
                        double angle;
                        if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
                            angle = Math.toRadians(-98);
                        } else {
                            angle = Math.toRadians(-84);
                        }
                        westCoast.startTurnGlobal(angle);
                        cycleState = CycleState.TURN_TO_SHOT_2;
                    }
                    break;
                case TURN_TO_SHOT_2:
                    if(westCoast.isInDefaultMode()){
                        shooter.runIndexerServos();
                        cycleState = CycleState.PSHOT_2;
                    }
                    break;
                case PSHOT_2:
                    if(shooter.areIndexerServosResting()){
                        double angle;
                        if(FTCUtilities.getAllianceColor().equals(AllianceColor.BLUE)){
                            angle = Math.toRadians(-103);
                        } else {
                            angle = Math.toRadians(-81);
                        }
                        westCoast.startTurnGlobal(angle);
                        cycleState = CycleState.TURN_TO_SHOT_3;
                    }
                    break;
                case TURN_TO_SHOT_3:
                    if(westCoast.isInDefaultMode()){
                        shooter.runIndexerServos();
                        shooter.setState(Shooter.ShooterState.POWERSHOT);
                        cycleState = CycleState.PSHOT_3;
                    }
                    break;
                    //powershot 3 merged with SHOOTING state

            }
        }

        if (state.equals(State.TURN_TO_SHOOT)){
            if (westCoast.isInDefaultMode()){
                shooter.runIndexerServos();
                setState(State.FREE);
            }
        }

        if (state.equals(State.GO_TO_HOME)){
            switch (homeState){
                case TURNING_TO_HOME:
                    if (westCoast.isInDefaultMode()){
                        intake.changeState(Intake.State.IN);

                        double distance = intakingPoint.distanceTo(odometry.getPosition());
                        westCoast.startDriveStraight(.9, distance, WestCoast.Direction.FORWARDS);

                        state = State.AUTO_CYCLE;
                        cycleState = CycleState.DRIVING_FORWARD;
                    }
                    break;
            }
        }

        if (state.equals(State.DETECTING_STACK)){
            if (FTCUtilities.getCurrentTimeMillis() - stackDetectorStartTime > 250){
                fieldConfiguration = stackDetector.getFieldConfiguration();
                stackDetectorStartTime = FTCUtilities.getCurrentTimeMillis();
            }
        }

        westCoast.update();
        wobbleArm.update();
        mb1242System.update();

        if(FTCUtilities.isDashboardMode()){
            dashboardTelemetry.update();
        }
        //todo track loop times
    }
}
